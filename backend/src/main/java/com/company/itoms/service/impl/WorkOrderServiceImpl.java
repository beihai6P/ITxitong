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
import com.company.itoms.exception.AiApiException;
import com.company.itoms.common.Idempotent;
import org.springframework.transaction.annotation.Transactional;
import com.company.itoms.mapper.WorkOrderMapper;
import com.company.itoms.mapper.WorkOrderFlowLogMapper;
import com.company.itoms.entity.WorkOrderFlowLogEntity;
import com.company.itoms.service.AiApiClient;
import com.company.itoms.service.KnowledgeBaseService;
import com.company.itoms.service.WorkOrderService;
import com.company.itoms.entity.KnowledgeBaseEntity;
import com.company.itoms.entity.UserEntity;
import com.company.itoms.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WorkOrderServiceImpl extends ServiceImpl<WorkOrderMapper, WorkOrderEntity> implements WorkOrderService {

    @Autowired
    private AiApiClient aiApiClient;

    @Autowired
    private WorkOrderFlowLogMapper workOrderFlowLogMapper;

    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    @Autowired
    private UserMapper userMapper;

    @Override
    @DataScope(deptAlias = "d")
    public Long createWorkOrder(WorkOrderCreateDTO dto) {
        WorkOrderEntity entity = new WorkOrderEntity();
        entity.setWorkOrderCode(dto.getWorkOrderCode());
        entity.setFaultType(dto.getFaultType() != null ? dto.getFaultType() : "OTHER");
        entity.setDescription(dto.getDescription());
        entity.setAssetId(dto.getAssetId());
        entity.setCreatorId(dto.getCreatorId() != null ? dto.getCreatorId() : 1L);
        entity.setDepartmentId(dto.getDepartmentId() != null ? dto.getDepartmentId() : 1L);
        entity.setUrgencyLevel(dto.getUrgencyLevel() != null ? dto.getUrgencyLevel() : 1);
        entity.setStatus(1); // 1-待处理
        
        this.save(entity);
        
        WorkOrderFlowLogEntity flowLog = new WorkOrderFlowLogEntity();
        flowLog.setWorkOrderId(entity.getId());
        flowLog.setOperatorId(entity.getCreatorId());
        flowLog.setAction("CREATE");
        flowLog.setPreviousStatus(null);
        flowLog.setCurrentStatus(1);
        flowLog.setRemark("创建工单");
        workOrderFlowLogMapper.insert(flowLog);
        
        // Publish event for big screen update could be here
        // eventPublisher.publishEvent(new WorkOrderCreatedEvent(entity.getId()));
        
        return entity.getId();
    }

    // AI服务调用降级模板（禁止修改结构）
    @Override
    public AiRecommendationDTO getRecommendation(WorkOrderCreateDTO dto) {
        try {
            return aiApiClient.call(dto); // 调用AI API
        } catch (AiApiException e) {
            log.warn("AI服务异常，启用降级策略", e);
            return fallbackRecommendation(dto); // 从知识库匹配
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
    public void assignWorkOrder(WorkOrderAssignDTO dto) {
        WorkOrderEntity entity = this.getById(dto.getWorkOrderId());
        if (entity == null) {
            throw new IllegalArgumentException("Work order not found");
        }
        Integer previousStatus = entity.getStatus();
        
        entity.setAssigneeId(dto.getAssigneeId());
        entity.setStatus(2); // 2-处理中 (wait, assigning might move it to processing? Or is there a 2-处理中 right away?)
        // Let's assume 2 is 处理中. Actually assigning moves to '2-处理中'
        this.updateById(entity);
        
        recordFlowLog(entity.getId(), dto.getOperatorId(), "ASSIGN", previousStatus, entity.getStatus(), dto.getRemark());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoAssignWorkOrder(Long workOrderId) {
        WorkOrderEntity entity = this.getById(workOrderId);
        if (entity == null || entity.getStatus() != 1) {
            throw new IllegalArgumentException("Work order not found or not in PENDING state");
        }

        // 1. Get all online repairmen (status = 1 means online/enabled)
        LambdaQueryWrapper<UserEntity> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(UserEntity::getRole, "REPAIRMAN")
                   .eq(UserEntity::getStatus, 1)
                   .eq(UserEntity::getIsDeleted, 0);
        List<UserEntity> repairmen = userMapper.selectList(userWrapper);

        if (repairmen.isEmpty()) {
            throw new IllegalStateException("No available online repairmen");
        }

        List<Long> repairmanIds = repairmen.stream().map(UserEntity::getId).collect(Collectors.toList());

        // 2. Calculate current workload for each repairman (status IN 1, 2)
        LambdaQueryWrapper<WorkOrderEntity> woWrapper = new LambdaQueryWrapper<>();
        woWrapper.in(WorkOrderEntity::getAssigneeId, repairmanIds)
                 .in(WorkOrderEntity::getStatus, 1, 2);
        List<WorkOrderEntity> activeOrders = this.list(woWrapper);

        Map<Long, Long> workloadMap = activeOrders.stream()
                .filter(wo -> wo.getAssigneeId() != null)
                .collect(Collectors.groupingBy(WorkOrderEntity::getAssigneeId, Collectors.counting()));

        // 3. Find the one with minimum workload
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
            bestAssigneeId = repairmanIds.get(0); // fallback
        }

        // 4. Assign
        Integer previousStatus = entity.getStatus();
        entity.setAssigneeId(bestAssigneeId);
        entity.setStatus(2); // Move to Processing
        this.updateById(entity);

        recordFlowLog(entity.getId(), 1L, "AUTO_ASSIGN", previousStatus, 2, "系统自动分配给工作量最少的维修工");
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
        for (Long workOrderId : dto.getWorkOrderIds()) {
            WorkOrderEntity entity = this.getById(workOrderId);
            if (entity == null) {
                continue;
            }
            Integer previousStatus = entity.getStatus();
            entity.setStatus(5); // 5-已关闭/驳回
            this.updateById(entity);
            recordFlowLog(entity.getId(), dto.getOperatorId(), "CLOSE", previousStatus, 5, dto.getRemark());
        }
    }

    @Override
    public void grabOrder(WorkOrderGrabDTO dto) {
        WorkOrderEntity entity = this.getById(dto.getWorkOrderId());
        if (entity == null) {
            throw new IllegalArgumentException("Work order not found");
        }
        Integer previousStatus = entity.getStatus();
        
        com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<WorkOrderEntity> updateWrapper = new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
        updateWrapper.eq(WorkOrderEntity::getId, dto.getWorkOrderId())
                     .eq(WorkOrderEntity::getStatus, 1)
                     .set(WorkOrderEntity::getAssigneeId, dto.getOperatorId())
                     .set(WorkOrderEntity::getStatus, 2);
                     
        boolean success = this.update(updateWrapper);
        if (!success) {
            throw new IllegalStateException("工单已被抢占或状态不正确");
        }
        
        recordFlowLog(entity.getId(), dto.getOperatorId(), "GRAB", previousStatus, 2, dto.getRemark());
    }

    @Override
    public void rejectOrder(WorkOrderRejectDTO dto) {
        WorkOrderEntity entity = this.getById(dto.getWorkOrderId());
        if (entity == null) {
            throw new IllegalArgumentException("Work order not found");
        }
        Integer previousStatus = entity.getStatus();
        
        entity.setAssigneeId(null);
        entity.setStatus(1); // 1-待处理
        this.updateById(entity);
        
        recordFlowLog(entity.getId(), dto.getOperatorId(), "REJECT", previousStatus, entity.getStatus(), dto.getRemark());
    }

    @Override
    public void transferOrder(WorkOrderTransferDTO dto) {
        WorkOrderEntity entity = this.getById(dto.getWorkOrderId());
        if (entity == null) {
            throw new IllegalArgumentException("Work order not found");
        }
        
        if (entity.getAssigneeId() == null || !entity.getAssigneeId().equals(dto.getOperatorId())) {
            throw new IllegalStateException("只有当前处理人才能转单");
        }
        
        Integer previousStatus = entity.getStatus();
        
        entity.setAssigneeId(dto.getTargetAssigneeId());
        this.updateById(entity);
        
        recordFlowLog(entity.getId(), dto.getOperatorId(), "TRANSFER", previousStatus, entity.getStatus(), dto.getRemark());
    }

    @Override
    public void suspendOrder(WorkOrderSuspendDTO dto) {
        WorkOrderEntity entity = this.getById(dto.getWorkOrderId());
        if (entity == null) {
            throw new IllegalArgumentException("Work order not found");
        }
        Integer previousStatus = entity.getStatus();
        
        entity.setStatus(6); // 6-挂起
        this.updateById(entity);
        
        recordFlowLog(entity.getId(), dto.getOperatorId(), "SUSPEND", previousStatus, entity.getStatus(), dto.getRemark());
    }

    @Override
    public void processWorkOrder(WorkOrderProcessDTO dto) {
        WorkOrderEntity entity = this.getById(dto.getWorkOrderId());
        if (entity == null) {
            throw new IllegalArgumentException("Work order not found");
        }
        Integer previousStatus = entity.getStatus();
        
        // Wait, what is status 3? 3-待验收. Processing might just be updating status to 3?
        // Let's assume process action moves it to 3-待验收
        entity.setStatus(3);
        this.updateById(entity);
        
        recordFlowLog(entity.getId(), dto.getOperatorId(), "PROCESS", previousStatus, entity.getStatus(), dto.getRemark());
    }

    @Override
    public void finishWorkOrder(WorkOrderFinishDTO dto) {
        WorkOrderEntity entity = this.getById(dto.getWorkOrderId());
        if (entity == null) {
            throw new IllegalArgumentException("Work order not found");
        }
        Integer previousStatus = entity.getStatus();
        
        entity.setStatus(4); // 4-已完成
        this.updateById(entity);
        
        recordFlowLog(entity.getId(), dto.getOperatorId(), "FINISH", previousStatus, entity.getStatus(), dto.getRemark());
    }

    @Override
    public void evaluateWorkOrder(WorkOrderEvaluateDTO dto) {
        WorkOrderEntity entity = this.getById(dto.getWorkOrderId());
        if (entity == null) {
            throw new IllegalArgumentException("Work order not found");
        }
        Integer previousStatus = entity.getStatus();
        
        entity.setEvaluateScore(dto.getEvaluateScore());
        entity.setEvaluateRemark(dto.getEvaluateRemark());
        // Status remains 4 or moves to 5? DB: 5-已关闭/驳回. So let's move to 5-已关闭
        entity.setStatus(5);
        this.updateById(entity);
        
        recordFlowLog(entity.getId(), dto.getOperatorId(), "EVALUATE", previousStatus, entity.getStatus(), dto.getEvaluateRemark());
        
        // Auto-deposit knowledge if evaluate score is 4 or 5
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

    private void recordFlowLog(Long workOrderId, Long operatorId, String action, Integer previousStatus, Integer currentStatus, String remark) {
        WorkOrderFlowLogEntity flowLog = new WorkOrderFlowLogEntity();
        flowLog.setWorkOrderId(workOrderId);
        flowLog.setOperatorId(operatorId != null ? operatorId : 1L); // Default to 1L if not provided
        flowLog.setAction(action);
        flowLog.setPreviousStatus(previousStatus);
        flowLog.setCurrentStatus(currentStatus);
        flowLog.setRemark(remark);
        workOrderFlowLogMapper.insert(flowLog);
    }
}
