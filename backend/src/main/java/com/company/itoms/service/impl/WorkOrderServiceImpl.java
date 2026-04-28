package com.company.itoms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.itoms.common.DataScope;
import com.company.itoms.dto.AiRecommendationDTO;
import com.company.itoms.dto.request.WorkOrderAssignDTO;
import com.company.itoms.dto.request.WorkOrderProcessDTO;
import com.company.itoms.dto.request.WorkOrderFinishDTO;
import com.company.itoms.dto.request.WorkOrderEvaluateDTO;
import com.company.itoms.dto.request.WorkOrderCreateDTO;
import com.company.itoms.dto.request.WorkOrderGrabDTO;
import com.company.itoms.dto.request.WorkOrderRejectDTO;
import com.company.itoms.dto.request.WorkOrderTransferDTO;
import com.company.itoms.dto.request.WorkOrderSuspendDTO;
import com.company.itoms.dto.request.WorkOrderBatchAssignDTO;
import com.company.itoms.dto.request.WorkOrderBatchCloseDTO;
import com.company.itoms.entity.WorkOrderEntity;
import com.company.itoms.entity.WorkOrderFlowLogEntity;
import com.company.itoms.entity.WorkOrderDispatchLogEntity;
import com.company.itoms.entity.EngineerWorkStatEntity;
import com.company.itoms.exception.AiApiException;
import com.company.itoms.exception.BusinessException;
import com.company.itoms.common.Idempotent;
import com.company.itoms.util.SecurityUtil;
import com.company.itoms.enums.WorkOrderStatusEnum;
import com.company.itoms.enums.AssetStatusEnum;
import org.springframework.transaction.annotation.Transactional;
import com.company.itoms.mapper.WorkOrderMapper;
import com.company.itoms.mapper.WorkOrderFlowLogMapper;
import com.company.itoms.mapper.WorkOrderDispatchLogMapper;
import com.company.itoms.mapper.EngineerWorkStatMapper;
import com.company.itoms.mapper.WorkOrderStatisticsMapper;
import com.company.itoms.service.AiApiClient;
import com.company.itoms.service.AssetService;
import com.company.itoms.service.KnowledgeBaseService;
import com.company.itoms.service.WorkOrderService;
import com.company.itoms.entity.AssetEntity;
import com.company.itoms.entity.KnowledgeBaseEntity;
import com.company.itoms.entity.UserEntity;
import com.company.itoms.mapper.AssetMapper;
import com.company.itoms.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WorkOrderServiceImpl extends ServiceImpl<WorkOrderMapper, WorkOrderEntity> implements WorkOrderService {

    @Autowired
    private WorkOrderMapper workOrderMapper;

    @Autowired
    private AiApiClient aiApiClient;

    @Autowired
    private WorkOrderFlowLogMapper workOrderFlowLogMapper;

    @Autowired
    private WorkOrderDispatchLogMapper workOrderDispatchLogMapper;

    @Autowired
    private EngineerWorkStatMapper engineerWorkStatMapper;

    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WorkOrderStatisticsMapper workOrderStatisticsMapper;

    @Autowired
    private AssetMapper assetMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createWorkOrder(WorkOrderCreateDTO dto) {
        WorkOrderEntity entity = new WorkOrderEntity();
        
        // 自动生成工单号
        if (dto.getWorkOrderCode() == null || dto.getWorkOrderCode().isEmpty()) {
            String timeStamp = String.valueOf(System.currentTimeMillis());
            entity.setWorkOrderCode("WO" + timeStamp);
        } else {
            entity.setWorkOrderCode(dto.getWorkOrderCode());
        }
        
        // 处理故障类型：数字转字符串
        String faultTypeStr;
        if (dto.getFaultType() != null) {
            switch (dto.getFaultType()) {
                case 1: faultTypeStr = "HARDWARE"; break;
                case 2: faultTypeStr = "SOFTWARE"; break;
                case 3: faultTypeStr = "NETWORK"; break;
                case 4: faultTypeStr = "SYSTEM"; break;
                case 5: faultTypeStr = "OTHER"; break;
                default: faultTypeStr = "OTHER";
            }
        } else {
            faultTypeStr = "OTHER";
        }
        entity.setFaultType(faultTypeStr);
        entity.setDescription(dto.getDescription());
        entity.setAssetId(dto.getAssetId());
        
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (dto.getCreatorId() != null) {
            entity.setCreatorId(dto.getCreatorId());
        } else if (currentUserId != null) {
            entity.setCreatorId(currentUserId);
        } else {
            throw new BusinessException(401, "未登录或登录已过期");
        }
        
        entity.setDepartmentId(dto.getDepartmentId() != null ? dto.getDepartmentId() : 1L);
        entity.setUrgencyLevel(dto.getUrgencyLevel() != null ? dto.getUrgencyLevel() : 1);
        entity.setContactPhone(dto.getContactPhone());
        entity.setStatus(WorkOrderStatusEnum.PENDING.code());
        entity.setProcessProgress(0);
        entity.setTimeoutMinutes(60);
        
        workOrderMapper.insert(entity);
        
        if (dto.getAssetId() != null) {
            updateAssetStatusOnRepair(dto.getAssetId());
        }
        
        WorkOrderFlowLogEntity flowLog = new WorkOrderFlowLogEntity();
        flowLog.setWorkOrderId(entity.getId());
        flowLog.setOperatorId(entity.getCreatorId());
        flowLog.setAction("CREATE");
        flowLog.setPreviousStatus(null);
        flowLog.setCurrentStatus(WorkOrderStatusEnum.PENDING.code());
        flowLog.setRemark("创建工单");
        workOrderFlowLogMapper.insert(flowLog);
        
        return entity.getId();
    }

    private void updateAssetStatusOnRepair(Long assetId) {
        AssetEntity asset = assetMapper.selectById(assetId);
        if (asset != null) {
            asset.setAssetStatus(AssetStatusEnum.REPAIR.code());
            asset.setRepairCount(asset.getRepairCount() != null ? asset.getRepairCount() + 1 : 1);
            assetMapper.updateById(asset);
            log.info("资产[{}]状态已更新为维修中，维修次数+1", asset.getAssetCode());
        }
    }

    private void restoreAssetStatusAfterRepair(Long assetId) {
        AssetEntity asset = assetMapper.selectById(assetId);
        if (asset != null) {
            asset.setAssetStatus(AssetStatusEnum.IN_USE.code());
            updateAssetHealthStatus(asset);
            assetMapper.updateById(asset);
            log.info("资产[{}]状态已恢复为在用", asset.getAssetCode());
        }
    }

    private void updateAssetHealthStatus(AssetEntity asset) {
    }

    @Override
    public AiRecommendationDTO getRecommendation(WorkOrderCreateDTO dto) {
        try {
            return aiApiClient.call(dto);
        } catch (AiApiException e) {
            log.warn("AI服务异常，启用降级策略", e);
            return fallbackRecommendation(dto);
        }
    }

    private AiRecommendationDTO fallbackRecommendation(WorkOrderCreateDTO dto) {
        AiRecommendationDTO recommendation = new AiRecommendationDTO();
        recommendation.setSolutionId(999L);
        recommendation.setConfidence(0.5);
        recommendation.setSource("KNOWLEDGE_BASE");
        return recommendation;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignWorkOrder(WorkOrderAssignDTO dto) {
        WorkOrderEntity entity = this.getById(dto.getWorkOrderId());
        if (entity == null) {
            throw new IllegalArgumentException("工单不存在");
        }
        
        WorkOrderStatusEnum currentStatus = WorkOrderStatusEnum.fromCode(entity.getStatus());
        WorkOrderStatusEnum targetStatus = WorkOrderStatusEnum.DISPATCHED;
        
        if (!currentStatus.canTransitionTo(targetStatus)) {
            throw new IllegalStateException("工单状态[" + currentStatus.name() + "]不允许执行指派操作");
        }
        
        Integer previousStatus = entity.getStatus();
        LocalDateTime now = LocalDateTime.now();
        
        entity.setAssigneeId(dto.getAssigneeId());
        entity.setDispatchEngineerId(dto.getAssigneeId());
        entity.setDispatchTime(now);
        entity.setStatus(targetStatus.code());
        this.updateById(entity);
        
        WorkOrderDispatchLogEntity dispatchLog = new WorkOrderDispatchLogEntity();
        dispatchLog.setOrderId(entity.getId());
        dispatchLog.setDispatchEngineerId(dto.getAssigneeId());
        dispatchLog.setDispatchTime(now);
        dispatchLog.setStatus(targetStatus.name());
        workOrderDispatchLogMapper.insert(dispatchLog);
        
        recordFlowLog(entity.getId(), dto.getOperatorId(), "ASSIGN", previousStatus, entity.getStatus(), dto.getRemark());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoAssignWorkOrder(Long workOrderId) {
        WorkOrderEntity entity = this.getById(workOrderId);
        if (entity == null) {
            throw new IllegalArgumentException("工单不存在");
        }
        WorkOrderStatusEnum currentStatus = WorkOrderStatusEnum.fromCode(entity.getStatus());
        if (currentStatus != WorkOrderStatusEnum.PENDING) {
            throw new IllegalArgumentException("工单状态[" + currentStatus.name() + "]不允许自动派单");
        }

        LambdaQueryWrapper<UserEntity> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(UserEntity::getRole, "REPAIRMAN")
                   .eq(UserEntity::getStatus, 1)
                   .eq(UserEntity::getIsDeleted, 0);
        List<UserEntity> repairmen = userMapper.selectList(userWrapper);

        if (repairmen.isEmpty()) {
            throw new IllegalStateException("No available online repairmen");
        }

        List<Long> repairmanIds = repairmen.stream().map(UserEntity::getId).collect(Collectors.toList());

        LambdaQueryWrapper<WorkOrderEntity> woWrapper = new LambdaQueryWrapper<>();
        woWrapper.in(WorkOrderEntity::getAssigneeId, repairmanIds)
                 .in(WorkOrderEntity::getStatus, 1, 2);
        List<WorkOrderEntity> activeOrders = this.list(woWrapper);

        Map<Long, Long> workloadMap = activeOrders.stream()
                .filter(wo -> wo.getAssigneeId() != null)
                .collect(Collectors.groupingBy(WorkOrderEntity::getAssigneeId, Collectors.counting()));

        Long bestAssigneeId = null;
        long minWorkload = Long.MAX_VALUE;

        for (Long id : repairmanIds) {
            long load = workloadMap.getOrDefault(id, 0L);
            if (load < minWorkload) {
                minWorkload = load;
                bestAssigneeId = id;
            }
        }

        if (bestAssigneeId == null) {
            bestAssigneeId = repairmanIds.get(0);
        }

        Integer previousStatus = entity.getStatus();
        entity.setAssigneeId(bestAssigneeId);
        entity.setStatus(2);
        this.updateById(entity);

        recordFlowLog(entity.getId(), 0L, "AUTO_ASSIGN", previousStatus, 2, "系统自动分配给工作量最少的维修工");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Idempotent
    public void batchAssign(WorkOrderBatchAssignDTO dto) {
        if (dto.getWorkOrderIds() == null || dto.getWorkOrderIds().isEmpty()) {
            return;
        }
        for (Long workOrderId : dto.getWorkOrderIds()) {
            WorkOrderAssignDTO assignDTO = new WorkOrderAssignDTO();
            assignDTO.setWorkOrderId(workOrderId);
            assignDTO.setAssigneeId(dto.getAssigneeId());
            assignDTO.setOperatorId(dto.getOperatorId());
            assignDTO.setRemark(dto.getRemark());
            this.assignWorkOrder(assignDTO);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Idempotent
    public void batchClose(WorkOrderBatchCloseDTO dto) {
        if (dto.getWorkOrderIds() == null || dto.getWorkOrderIds().isEmpty()) {
            return;
        }
        WorkOrderStatusEnum targetStatus = WorkOrderStatusEnum.CLOSED;
        for (Long workOrderId : dto.getWorkOrderIds()) {
            WorkOrderEntity entity = this.getById(workOrderId);
            if (entity == null) {
                continue;
            }
            WorkOrderStatusEnum currentStatus = WorkOrderStatusEnum.fromCode(entity.getStatus());
            if (!currentStatus.canTransitionTo(targetStatus)) {
                log.warn("工单[{}]状态[{}]不允许关闭", workOrderId, currentStatus.name());
                continue;
            }
            Integer previousStatus = entity.getStatus();
            entity.setStatus(targetStatus.code());
            this.updateById(entity);
            recordFlowLog(entity.getId(), dto.getOperatorId(), "CLOSE", previousStatus, targetStatus.code(), dto.getRemark());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grabOrder(WorkOrderGrabDTO dto) {
        WorkOrderEntity entity = this.getById(dto.getWorkOrderId());
        if (entity == null) {
            throw new IllegalArgumentException("工单不存在");
        }
        
        WorkOrderStatusEnum currentStatus = WorkOrderStatusEnum.fromCode(entity.getStatus());
        WorkOrderStatusEnum targetStatus = WorkOrderStatusEnum.PROCESSING;
        
        if (!currentStatus.canTransitionTo(targetStatus)) {
            throw new IllegalStateException("工单状态[" + currentStatus.name() + "]不允许抢单");
        }
        
        Integer previousStatus = entity.getStatus();
        LocalDateTime now = LocalDateTime.now();
        
        com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<WorkOrderEntity> updateWrapper = new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
        updateWrapper.eq(WorkOrderEntity::getId, dto.getWorkOrderId())
                     .eq(WorkOrderEntity::getStatus, currentStatus.code())
                     .set(WorkOrderEntity::getAssigneeId, dto.getOperatorId())
                     .set(WorkOrderEntity::getAcceptEngineerId, dto.getOperatorId())
                     .set(WorkOrderEntity::getAcceptTime, now)
                     .set(WorkOrderEntity::getStatus, targetStatus.code());
                      
        boolean success = this.update(updateWrapper);
        if (!success) {
            throw new IllegalStateException("工单已被抢占或状态已变更");
        }
        
        WorkOrderDispatchLogEntity dispatchLog = new WorkOrderDispatchLogEntity();
        dispatchLog.setOrderId(entity.getId());
        dispatchLog.setDispatchEngineerId(dto.getOperatorId());
        dispatchLog.setAcceptEngineerId(dto.getOperatorId());
        dispatchLog.setDispatchTime(now);
        dispatchLog.setAcceptTime(now);
        dispatchLog.setStatus("已接单");
        workOrderDispatchLogMapper.insert(dispatchLog);
        
        recordFlowLog(entity.getId(), dto.getOperatorId(), "GRAB", previousStatus, targetStatus.code(), "工程师抢单成功");
    }

    @Override
    public void rejectOrder(WorkOrderRejectDTO dto) {
        WorkOrderEntity entity = this.getById(dto.getWorkOrderId());
        if (entity == null) {
            throw new IllegalArgumentException("工单不存在");
        }
        
        WorkOrderStatusEnum currentStatus = WorkOrderStatusEnum.fromCode(entity.getStatus());
        WorkOrderStatusEnum targetStatus = WorkOrderStatusEnum.REJECTED;
        
        if (!currentStatus.canTransitionTo(targetStatus)) {
            throw new IllegalStateException("工单状态[" + currentStatus.name() + "]不允许驳回");
        }
        
        Integer previousStatus = entity.getStatus();
        
        entity.setAssigneeId(null);
        entity.setStatus(targetStatus.code());
        this.updateById(entity);
        
        recordFlowLog(entity.getId(), dto.getOperatorId(), "REJECT", previousStatus, entity.getStatus(), dto.getRemark());
    }

    @Override
    public void transferOrder(WorkOrderTransferDTO dto) {
        WorkOrderEntity entity = this.getById(dto.getWorkOrderId());
        if (entity == null) {
            throw new IllegalArgumentException("工单不存在");
        }
        
        if (entity.getAssigneeId() == null || !entity.getAssigneeId().equals(dto.getOperatorId())) {
            throw new IllegalStateException("只有当前处理人才能转单");
        }
        
        Integer previousStatus = entity.getStatus();
        
        entity.setAssigneeId(dto.getTargetAssigneeId());
        entity.setStatus(WorkOrderStatusEnum.DISPATCHED.code());
        this.updateById(entity);
        
        recordFlowLog(entity.getId(), dto.getOperatorId(), "TRANSFER", previousStatus, entity.getStatus(), dto.getRemark());
    }

    @Override
    public void suspendOrder(WorkOrderSuspendDTO dto) {
        WorkOrderEntity entity = this.getById(dto.getWorkOrderId());
        if (entity == null) {
            throw new IllegalArgumentException("工单不存在");
        }
        
        WorkOrderStatusEnum currentStatus = WorkOrderStatusEnum.fromCode(entity.getStatus());
        WorkOrderStatusEnum targetStatus = WorkOrderStatusEnum.SUSPENDED;
        
        if (!currentStatus.canTransitionTo(targetStatus)) {
            throw new IllegalStateException("工单状态[" + currentStatus.name() + "]不允许暂停");
        }
        
        Integer previousStatus = entity.getStatus();
        
        entity.setStatus(targetStatus.code());
        this.updateById(entity);
        
        recordFlowLog(entity.getId(), dto.getOperatorId(), "SUSPEND", previousStatus, entity.getStatus(), dto.getRemark());
    }

    @Override
    public void processWorkOrder(WorkOrderProcessDTO dto) {
        WorkOrderEntity entity = this.getById(dto.getWorkOrderId());
        if (entity == null) {
            throw new IllegalArgumentException("工单不存在");
        }
        
        WorkOrderStatusEnum currentStatus = WorkOrderStatusEnum.fromCode(entity.getStatus());
        WorkOrderStatusEnum targetStatus = WorkOrderStatusEnum.PROCESSING;
        
        if (!currentStatus.canTransitionTo(targetStatus)) {
            throw new IllegalStateException("工单状态[" + currentStatus.name() + "]不允许开始处理");
        }
        
        Integer previousStatus = entity.getStatus();
        
        entity.setStatus(targetStatus.code());
        this.updateById(entity);
        
        recordFlowLog(entity.getId(), dto.getOperatorId(), "PROCESS", previousStatus, entity.getStatus(), dto.getRemark());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finishWorkOrder(WorkOrderFinishDTO dto) {
        WorkOrderEntity entity = this.getById(dto.getWorkOrderId());
        if (entity == null) {
            throw new IllegalArgumentException("工单不存在");
        }
        
        WorkOrderStatusEnum currentStatus = WorkOrderStatusEnum.fromCode(entity.getStatus());
        WorkOrderStatusEnum targetStatus = WorkOrderStatusEnum.PENDING_ACCEPTANCE;
        
        if (!currentStatus.canTransitionTo(targetStatus)) {
            throw new IllegalStateException("工单状态[" + currentStatus.name() + "]不允许完成");
        }
        
        Integer previousStatus = entity.getStatus();
        
        entity.setStatus(targetStatus.code());
        this.updateById(entity);
        
        recordFlowLog(entity.getId(), dto.getOperatorId(), "FINISH", previousStatus, entity.getStatus(), dto.getRemark());
    }

    @Override
    public void evaluateWorkOrder(WorkOrderEvaluateDTO dto) {
        WorkOrderEntity entity = this.getById(dto.getWorkOrderId());
        if (entity == null) {
            throw new IllegalArgumentException("工单不存在");
        }
        
        WorkOrderStatusEnum currentStatus = WorkOrderStatusEnum.fromCode(entity.getStatus());
        WorkOrderStatusEnum targetStatus = WorkOrderStatusEnum.COMPLETED;
        
        if (!currentStatus.canTransitionTo(targetStatus)) {
            throw new IllegalStateException("工单状态[" + currentStatus.name() + "]不允许验收");
        }
        
        Integer previousStatus = entity.getStatus();
        
        entity.setEvaluateScore(dto.getEvaluateScore());
        entity.setEvaluateRemark(dto.getEvaluateRemark());
        entity.setStatus(targetStatus.code());
        this.updateById(entity);
        
        recordFlowLog(entity.getId(), dto.getOperatorId(), "EVALUATE", previousStatus, entity.getStatus(), dto.getEvaluateRemark());
        
        if (entity.getAssetId() != null) {
            restoreAssetStatusAfterRepair(entity.getAssetId());
        }
        
        if (dto.getEvaluateScore() != null && dto.getEvaluateScore() >= 4) {
            LambdaQueryWrapper<WorkOrderFlowLogEntity> query = new LambdaQueryWrapper<>();
            query.eq(WorkOrderFlowLogEntity::getWorkOrderId, entity.getId())
                 .eq(WorkOrderFlowLogEntity::getAction, "FINISH")
                 .orderByDesc(WorkOrderFlowLogEntity::getId)
                 .last("LIMIT 1");
            WorkOrderFlowLogEntity finishLog = workOrderFlowLogMapper.selectOne(query);
            
            String solution = finishLog != null ? finishLog.getRemark() : "工单自动沉淀知识";
            
            KnowledgeBaseEntity kb = new KnowledgeBaseEntity();
            String title = entity.getDescription() != null && entity.getDescription().length() > 50 
                    ? entity.getDescription().substring(0, 50) + "..." 
                    : entity.getDescription();
            kb.setTitle(title != null ? title : "未知故障");
            kb.setContent("问题描述: " + entity.getDescription() + "\n\n解决方案: " + solution);
            kb.setFaultType(entity.getFaultType());
            knowledgeBaseService.save(kb);
        }
    }

    @Override
    public List<WorkOrderEntity> getWorkOrderList(Integer status, Integer type, Integer priority, Long engineerId, Integer isTimeout, Boolean isMyOrder, Long currentUserId) {
        try {
            LambdaQueryWrapper<WorkOrderEntity> wrapper = new LambdaQueryWrapper<>();
            if (status != null) {
                wrapper.eq(WorkOrderEntity::getStatus, status);
            }
            if (type != null) {
                // 将数字类型转换为字符串类型进行查询
                String faultTypeStr;
                switch (type) {
                    case 1: faultTypeStr = "HARDWARE"; break;
                    case 2: faultTypeStr = "SOFTWARE"; break;
                    case 3: faultTypeStr = "NETWORK"; break;
                    case 4: faultTypeStr = "SYSTEM"; break;
                    case 5: faultTypeStr = "OTHER"; break;
                    default: faultTypeStr = type.toString();
                }
                wrapper.eq(WorkOrderEntity::getFaultType, faultTypeStr);
            }
            if (priority != null) {
                wrapper.eq(WorkOrderEntity::getUrgencyLevel, priority);
            }
            if (engineerId != null) {
                wrapper.eq(WorkOrderEntity::getAssigneeId, engineerId);
            }
            if (isTimeout != null) {
                LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(60);
                if (isTimeout == 1) {
                    wrapper.lt(WorkOrderEntity::getCreateTime, timeoutTime)
                           .notIn(WorkOrderEntity::getStatus, 4, 5, 6);
                } else {
                    wrapper.gt(WorkOrderEntity::getCreateTime, timeoutTime)
                           .or()
                           .in(WorkOrderEntity::getStatus, 4, 5, 6);
                }
            }
            if (Boolean.TRUE.equals(isMyOrder) && currentUserId != null) {
                wrapper.eq(WorkOrderEntity::getAssigneeId, currentUserId);
            }
            wrapper.orderByDesc(WorkOrderEntity::getCreateTime);
            
            List<WorkOrderEntity> list = this.list(wrapper);
            
            // 设置虚拟字段：工程师姓名等
            for (WorkOrderEntity order : list) {
                if (order.getDispatchEngineerId() != null) {
                    UserEntity dispatchUser = userMapper.selectById(order.getDispatchEngineerId());
                    if (dispatchUser != null) {
                        order.setDispatchEngineerName(dispatchUser.getRealName());
                    }
                }
                if (order.getAcceptEngineerId() != null) {
                    UserEntity acceptUser = userMapper.selectById(order.getAcceptEngineerId());
                    if (acceptUser != null) {
                        order.setAcceptEngineerName(acceptUser.getRealName());
                    }
                }
            }
            
            return list;
        } catch (Exception e) {
            log.error("获取工单列表失败", e);
            return new ArrayList<>();
        }}

    @Override
    public Map<String, Object> getWorkOrderStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            LocalDate today = LocalDate.now();
            LocalDateTime todayStart = today.atStartOfDay();
            LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(60);
            
            // 使用专门的统计Mapper，绕过数据权限过滤
            long total = workOrderStatisticsMapper.countAll();
            long todayNew = workOrderStatisticsMapper.countTodayNew(todayStart.toString());
            long waitDispatch = workOrderStatisticsMapper.countWaitDispatch();
            long waitAccept = workOrderStatisticsMapper.countWaitAccept();
            long processing = workOrderStatisticsMapper.countProcessing();
            long completed = workOrderStatisticsMapper.countCompleted();
            long timeout = workOrderStatisticsMapper.countTimeout(timeoutTime.toString());
            
            // 闭环率
            double closeRate = total > 0 ? (double) completed / total * 100 : 0;
            
            stats.put("total", total);
            stats.put("todayNew", todayNew);
            stats.put("waitDispatch", waitDispatch);
            stats.put("waitAccept", waitAccept);
            stats.put("processing", processing);
            stats.put("completed", completed);
            stats.put("timeout", timeout);
            stats.put("closeRate", String.format("%.1f%%", closeRate));
            return stats;
        } catch (Exception e) {
            log.error("获取工单统计失败", e);
            Map<String, Object> fallbackStats = new HashMap<>();
            fallbackStats.put("total", 0L);
            fallbackStats.put("todayNew", 0L);
            fallbackStats.put("waitDispatch", 0L);
            fallbackStats.put("waitAccept", 0L);
            fallbackStats.put("processing", 0L);
            fallbackStats.put("completed", 0L);
            fallbackStats.put("timeout", 0L);
            fallbackStats.put("closeRate", "0.0%");
            return fallbackStats;
        }
    }

    @Override
    public List<Map<String, Object>> getWorkOrderTrend() {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
            LocalDateTime now = LocalDateTime.now();
            String startDate = now.minusDays(6).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            
            // 使用专门的统计Mapper，绕过数据权限过滤
            List<Map<String, Object>> rawData = workOrderStatisticsMapper.countByDay(startDate);
            
            Map<String, Long> dateCountMap = new HashMap<>();
            for (int i = 6; i >= 0; i--) {
                String date = now.minusDays(i).format(formatter);
                dateCountMap.put(date, 0L);
            }
            
            for (Map<String, Object> item : rawData) {
                String date = item.get("date").toString();
                if (dateCountMap.containsKey(date)) {
                    dateCountMap.put(date, ((Number) item.get("count")).longValue());
                }
            }
            
            List<Map<String, Object>> result = new ArrayList<>();
            for (int i = 6; i >= 0; i--) {
                String date = now.minusDays(i).format(formatter);
                Map<String, Object> item = new HashMap<>();
                item.put("date", date);
                item.put("count", dateCountMap.getOrDefault(date, 0L));
                result.add(item);
            }
            
            return result;
        } catch (Exception e) {
            log.error("获取工单趋势失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> getTypeDistribution() {
        try {
            List<WorkOrderEntity> allOrders = this.list();
            Map<String, Integer> typeCountMap = new HashMap<>();
            
            for (WorkOrderEntity order : allOrders) {
                String type = order.getFaultType() != null ? order.getFaultType() : "OTHER";
                typeCountMap.put(type, typeCountMap.getOrDefault(type, 0) + 1);
            }
            
            List<Map<String, Object>> result = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : typeCountMap.entrySet()) {
                Map<String, Object> item = new HashMap<>();
                
                // 将字符串类型转换为数字类型，兼容前端显示
                Integer typeNum;
                switch (entry.getKey()) {
                    case "HARDWARE": typeNum = 1; break;
                    case "SOFTWARE": typeNum = 2; break;
                    case "NETWORK": typeNum = 3; break;
                    case "SYSTEM": typeNum = 4; break;
                    case "OTHER": typeNum = 5; break;
                    default: typeNum = 5;
                }
                
                item.put("type", typeNum);
                item.put("count", entry.getValue());
                result.add(item);
            }
            
            return result;
        } catch (Exception e) {
            log.error("获取故障类型分布失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> getWorkOrderLogs(Long id) {
        try {
            LambdaQueryWrapper<WorkOrderFlowLogEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(WorkOrderFlowLogEntity::getWorkOrderId, id);
            wrapper.orderByAsc(WorkOrderFlowLogEntity::getCreateTime);
            List<WorkOrderFlowLogEntity> logs = workOrderFlowLogMapper.selectList(wrapper);
            
            List<Map<String, Object>> result = new ArrayList<>();
            for (WorkOrderFlowLogEntity logEntity : logs) {
                Map<String, Object> item = new HashMap<>();
                item.put("time", logEntity.getCreateTime() != null ? logEntity.getCreateTime().toString() : "");
                item.put("content", logEntity.getRemark() != null ? logEntity.getRemark() : "");
                item.put("operator", logEntity.getOperatorId() != null ? logEntity.getOperatorId() : "系统");
                item.put("type", "primary");
                result.add(item);
            }
            
            return result;
        } catch (Exception e) {
            log.error("获取工单日志失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> getAssignees() {
        try {
            LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserEntity::getRole, "REPAIRMAN")
                   .eq(UserEntity::getStatus, 1)
                   .eq(UserEntity::getIsDeleted, 0);
            List<UserEntity> users = userMapper.selectList(wrapper);
            
            List<Map<String, Object>> result = new ArrayList<>();
            for (UserEntity user : users) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", user.getId());
                item.put("name", user.getUsername() != null ? user.getUsername() : "");
                result.add(item);
            }
            
            return result;
        } catch (Exception e) {
            log.error("获取处理人列表失败", e);
            return new ArrayList<>();
        }
    }

    private void recordFlowLog(Long workOrderId, Long operatorId, String action, Integer previousStatus, Integer currentStatus, String remark) {
        WorkOrderFlowLogEntity flowLog = new WorkOrderFlowLogEntity();
        flowLog.setWorkOrderId(workOrderId);
        flowLog.setOperatorId(operatorId != null ? operatorId : 1L);
        flowLog.setAction(action);
        flowLog.setPreviousStatus(previousStatus);
        flowLog.setCurrentStatus(currentStatus);
        flowLog.setRemark(remark);
        workOrderFlowLogMapper.insert(flowLog);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean autoDispatchWorkOrders() {
        try {
            // 查找待派单的工单
            LambdaQueryWrapper<WorkOrderEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(WorkOrderEntity::getStatus, 1); // 1-待处理
            List<WorkOrderEntity> pendingOrders = this.list(wrapper);

            if (pendingOrders.isEmpty()) {
                return true;
            }

            // 获取可用工程师
            LambdaQueryWrapper<UserEntity> userWrapper = new LambdaQueryWrapper<>();
            userWrapper.eq(UserEntity::getRole, "REPAIRMAN")
                       .eq(UserEntity::getStatus, 1)
                       .eq(UserEntity::getIsDeleted, 0);
            List<UserEntity> engineers = userMapper.selectList(userWrapper);

            if (engineers.isEmpty()) {
                log.warn("无可用工程师");
                return false;
            }

            // 计算工程师负载
            Map<Long, Long> workloadMap = new HashMap<>();
            LambdaQueryWrapper<WorkOrderEntity> activeWrapper = new LambdaQueryWrapper<>();
            activeWrapper.in(WorkOrderEntity::getStatus, 2, 3) // 处理中
                       .in(WorkOrderEntity::getAssigneeId, engineers.stream().map(UserEntity::getId).collect(Collectors.toList()));
            List<WorkOrderEntity> activeOrders = this.list(activeWrapper);
            for (WorkOrderEntity order : activeOrders) {
                if (order.getAssigneeId() != null) {
                    workloadMap.merge(order.getAssigneeId(), 1L, Long::sum);
                }
            }

            // 自动派单
            for (WorkOrderEntity order : pendingOrders) {
                // 选择最优工程师
                Long bestEngineerId = null;
                long minWorkload = Long.MAX_VALUE;
                for (UserEntity engineer : engineers) {
                    long load = workloadMap.getOrDefault(engineer.getId(), 0L);
                    if (load < minWorkload) {
                        minWorkload = load;
                        bestEngineerId = engineer.getId();
                    }
                }

                if (bestEngineerId != null) {
                    // 使用乐观锁更新工单状态，确保并发安全
                    com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<WorkOrderEntity> updateWrapper = new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
                    updateWrapper.eq(WorkOrderEntity::getId, order.getId())
                                 .eq(WorkOrderEntity::getStatus, 1) // 确保状态还是待处理
                                 .set(WorkOrderEntity::getDispatchEngineerId, bestEngineerId)
                                 .set(WorkOrderEntity::getStatus, 2) // 已派单
                                 .set(WorkOrderEntity::getDispatchTime, LocalDateTime.now());
                    
                    boolean success = this.update(updateWrapper);
                    if (success) {
                        // 记录派单日志
                        WorkOrderDispatchLogEntity dispatchLog = new WorkOrderDispatchLogEntity();
                        dispatchLog.setOrderId(order.getId());
                        dispatchLog.setDispatchEngineerId(bestEngineerId);
                        dispatchLog.setDispatchTime(LocalDateTime.now());
                        dispatchLog.setStatus("已派单");
                        workOrderDispatchLogMapper.insert(dispatchLog);

                        // 记录流转日志
                        recordFlowLog(order.getId(), 1L, "AUTO_DISPATCH", 1, 2, "系统自动派单给工程师" + bestEngineerId);

                        // 更新负载
                        workloadMap.merge(bestEngineerId, 1L, Long::sum);
                    }
                }
            }

            return true;
        } catch (Exception e) {
            log.error("自动派单失败", e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean manualDispatch(Long workOrderId, Long engineerId, Long operatorId, String operatorName) {
        try {
            WorkOrderEntity order = this.getById(workOrderId);
            if (order == null || order.getStatus() != 1) {
                throw new IllegalArgumentException("工单不存在或状态不正确");
            }

            order.setDispatchEngineerId(engineerId);
            order.setAssigneeId(engineerId); // 设置处理人ID，确保工单关联到工程师
            order.setStatus(2);
            order.setDispatchTime(LocalDateTime.now());
            this.updateById(order);

            // 记录派单日志
            WorkOrderDispatchLogEntity dispatchLog = new WorkOrderDispatchLogEntity();
            dispatchLog.setOrderId(workOrderId);
            dispatchLog.setDispatchEngineerId(engineerId);
            dispatchLog.setDispatchTime(LocalDateTime.now());
            dispatchLog.setStatus("已派单");
            workOrderDispatchLogMapper.insert(dispatchLog);

            // 记录流转日志
            recordFlowLog(workOrderId, operatorId, "MANUAL_DISPATCH", 1, 2, operatorName + "手动派单给工程师" + engineerId);

            return true;
        } catch (Exception e) {
            log.error("手动派单失败", e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean acceptWorkOrder(Long workOrderId, Long engineerId, Long operatorId, String operatorName) {
        try {
            WorkOrderEntity order = this.getById(workOrderId);
            if (order == null || order.getStatus() != 2) {
                throw new IllegalArgumentException("工单不存在或状态不正确");
            }

            order.setAcceptEngineerId(engineerId);
            order.setAssigneeId(engineerId);
            order.setStatus(3); // 处理中
            order.setAcceptTime(LocalDateTime.now());
            this.updateById(order);

            // 更新派单日志
            LambdaQueryWrapper<WorkOrderDispatchLogEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(WorkOrderDispatchLogEntity::getOrderId, workOrderId)
                   .eq(WorkOrderDispatchLogEntity::getStatus, "已派单")
                   .orderByDesc(WorkOrderDispatchLogEntity::getId)
                   .last("LIMIT 1");
            WorkOrderDispatchLogEntity dispatchLog = workOrderDispatchLogMapper.selectOne(wrapper);
            if (dispatchLog != null) {
                dispatchLog.setAcceptEngineerId(engineerId);
                dispatchLog.setAcceptTime(LocalDateTime.now());
                dispatchLog.setStatus("已接单");
                workOrderDispatchLogMapper.updateById(dispatchLog);
            }

            // 记录流转日志
            recordFlowLog(workOrderId, operatorId, "ACCEPT", 2, 3, operatorName + "接单处理");

            return true;
        } catch (Exception e) {
            log.error("接单失败", e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean refuseWorkOrder(Long workOrderId, Long engineerId, String reason, Long operatorId, String operatorName) {
        try {
            WorkOrderEntity order = this.getById(workOrderId);
            if (order == null || order.getStatus() != 2) {
                throw new IllegalArgumentException("工单不存在或状态不正确");
            }

            order.setStatus(1); // 待处理
            this.updateById(order);

            // 更新派单日志
            LambdaQueryWrapper<WorkOrderDispatchLogEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(WorkOrderDispatchLogEntity::getOrderId, workOrderId)
                   .eq(WorkOrderDispatchLogEntity::getStatus, "已派单")
                   .orderByDesc(WorkOrderDispatchLogEntity::getId)
                   .last("LIMIT 1");
            WorkOrderDispatchLogEntity dispatchLog = workOrderDispatchLogMapper.selectOne(wrapper);
            if (dispatchLog != null) {
                dispatchLog.setStatus("已拒单");
                dispatchLog.setRefuseReason(reason);
                workOrderDispatchLogMapper.updateById(dispatchLog);
            }

            // 记录流转日志
            recordFlowLog(workOrderId, operatorId, "REFUSE", 2, 1, operatorName + "拒单: " + reason);

            return true;
        } catch (Exception e) {
            log.error("拒单失败", e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean transferWorkOrder(Long workOrderId, Long newEngineerId, Long operatorId, String operatorName) {
        try {
            WorkOrderEntity order = this.getById(workOrderId);
            if (order == null) {
                throw new IllegalArgumentException("工单不存在");
            }

            order.setDispatchEngineerId(newEngineerId);
            order.setAssigneeId(newEngineerId);
            order.setStatus(2); // 已派单
            order.setDispatchTime(LocalDateTime.now());
            this.updateById(order);

            // 记录新的派单日志
            WorkOrderDispatchLogEntity dispatchLog = new WorkOrderDispatchLogEntity();
            dispatchLog.setOrderId(workOrderId);
            dispatchLog.setDispatchEngineerId(newEngineerId);
            dispatchLog.setDispatchTime(LocalDateTime.now());
            dispatchLog.setStatus("已派单");
            workOrderDispatchLogMapper.insert(dispatchLog);

            // 记录流转日志
            recordFlowLog(workOrderId, operatorId, "TRANSFER", order.getStatus(), 2, operatorName + "转单给工程师" + newEngineerId);

            return true;
        } catch (Exception e) {
            log.error("转单失败", e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateProcessProgress(Long workOrderId, Integer progress, Long operatorId, String operatorName) {
        try {
            WorkOrderEntity order = this.getById(workOrderId);
            if (order == null) {
                throw new IllegalArgumentException("工单不存在");
            }

            order.setProcessProgress(progress);
            this.updateById(order);

            // 记录流转日志
            recordFlowLog(workOrderId, operatorId, "UPDATE_PROGRESS", order.getStatus(), order.getStatus(), operatorName + "更新进度至" + progress + "%");

            return true;
        } catch (Exception e) {
            log.error("更新进度失败", e);
            return false;
        }
    }

    @Override
    public List<WorkOrderDispatchLogEntity> getDispatchLogs(Long workOrderId) {
        try {
            LambdaQueryWrapper<WorkOrderDispatchLogEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(WorkOrderDispatchLogEntity::getOrderId, workOrderId);
            wrapper.orderByDesc(WorkOrderDispatchLogEntity::getCreateTime);
            List<WorkOrderDispatchLogEntity> logs = workOrderDispatchLogMapper.selectList(wrapper);

            // 填充工程师姓名
            for (WorkOrderDispatchLogEntity log : logs) {
                if (log.getDispatchEngineerId() != null) {
                    UserEntity engineer = userMapper.selectById(log.getDispatchEngineerId());
                    if (engineer != null) {
                        log.setDispatchEngineerName(engineer.getUsername());
                    }
                }
                if (log.getAcceptEngineerId() != null) {
                    UserEntity engineer = userMapper.selectById(log.getAcceptEngineerId());
                    if (engineer != null) {
                        log.setAcceptEngineerName(engineer.getUsername());
                    }
                }
            }

            return logs;
        } catch (Exception e) {
            log.error("获取派单记录失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<EngineerWorkStatEntity> getEngineerWorkStats(LocalDate startDate, LocalDate endDate) {
        try {
            LambdaQueryWrapper<EngineerWorkStatEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.between(EngineerWorkStatEntity::getStatDate, startDate, endDate);
            wrapper.orderByDesc(EngineerWorkStatEntity::getStatDate);
            return engineerWorkStatMapper.selectList(wrapper);
        } catch (Exception e) {
            log.error("获取工程师工作量统计失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean generateEngineerWorkStats(LocalDate date) {
        try {
            // 清空当天的统计数据
            LambdaQueryWrapper<EngineerWorkStatEntity> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(EngineerWorkStatEntity::getStatDate, date);
            engineerWorkStatMapper.delete(deleteWrapper);

            // 获取所有工程师
            LambdaQueryWrapper<UserEntity> userWrapper = new LambdaQueryWrapper<>();
            userWrapper.eq(UserEntity::getRole, "REPAIRMAN")
                       .eq(UserEntity::getIsDeleted, 0);
            List<UserEntity> engineers = userMapper.selectList(userWrapper);

            for (UserEntity engineer : engineers) {
                EngineerWorkStatEntity stat = new EngineerWorkStatEntity();
                stat.setEngineerId(engineer.getId());
                stat.setEngineerName(engineer.getUsername());
                stat.setStatDate(date);

                // 统计总工单数
                LambdaQueryWrapper<WorkOrderEntity> totalWrapper = new LambdaQueryWrapper<>();
                totalWrapper.eq(WorkOrderEntity::getAssigneeId, engineer.getId());
                totalWrapper.apply("DATE(create_time) = DATE('" + date + "')");
                stat.setTotalOrder((int) this.count(totalWrapper));

                // 统计接单数
                LambdaQueryWrapper<WorkOrderEntity> acceptWrapper = new LambdaQueryWrapper<>();
                acceptWrapper.eq(WorkOrderEntity::getAcceptEngineerId, engineer.getId());
                acceptWrapper.apply("DATE(accept_time) = DATE('" + date + "')");
                stat.setAcceptOrder((int) this.count(acceptWrapper));

                // 统计完成数
                LambdaQueryWrapper<WorkOrderEntity> finishWrapper = new LambdaQueryWrapper<>();
                finishWrapper.eq(WorkOrderEntity::getAssigneeId, engineer.getId());
                finishWrapper.in(WorkOrderEntity::getStatus, 4, 5);
                finishWrapper.apply("DATE(update_time) = DATE('" + date + "')");
                stat.setFinishOrder((int) this.count(finishWrapper));

                // 计算完成率
                if (stat.getTotalOrder() > 0) {
                    double finishRate = (double) stat.getFinishOrder() / stat.getTotalOrder() * 100;
                    stat.setFinishRate(java.math.BigDecimal.valueOf(finishRate).setScale(2, java.math.RoundingMode.HALF_UP));
                } else {
                    stat.setFinishRate(java.math.BigDecimal.ZERO);
                }

                engineerWorkStatMapper.insert(stat);
            }

            return true;
        } catch (Exception e) {
            log.error("生成工程师工作量统计失败", e);
            return false;
        }
    }

    @Override
    public List<Map<String, Object>> getEngineerWorkloadDistribution() {
        try {
            // 使用专门的统计Mapper，绕过数据权限过滤
            return workOrderStatisticsMapper.countByEngineer();
        } catch (Exception e) {
            log.error("获取工程师工作量分布失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> getWorkOrderStatusFunnel() {
        try {
            // 使用专门的统计Mapper，绕过数据权限过滤
            List<Map<String, Object>> rawData = workOrderStatisticsMapper.countByStatus();

            Map<String, String> statusMap = new HashMap<>();
            statusMap.put("1", "待处理");
            statusMap.put("2", "已派单");
            statusMap.put("3", "处理中");
            statusMap.put("4", "已完成");
            statusMap.put("5", "已关闭");

            List<Map<String, Object>> result = new ArrayList<>();
            for (Map.Entry<String, String> entry : statusMap.entrySet()) {
                long count = 0;
                for (Map<String, Object> item : rawData) {
                    if (entry.getKey().equals(item.get("status").toString())) {
                        count = ((Number) item.get("value")).longValue();
                        break;
                    }
                }

                Map<String, Object> funnelItem = new HashMap<>();
                funnelItem.put("name", entry.getValue());
                funnelItem.put("value", count);
                result.add(funnelItem);
            }

            return result;
        } catch (Exception e) {
            log.error("获取工单状态漏斗失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> getEngineerStats(LocalDate startDate, LocalDate endDate) {
        try {
            // 使用专门的统计Mapper，绕过数据权限过滤
            return workOrderStatisticsMapper.countEngineerStats();
        } catch (Exception e) {
            log.error("获取工程师统计失败", e);
            return new ArrayList<>();
        }
    }

}

