package com.company.itoms.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.itoms.enums.WorkOrderStatusEnum;
import com.company.itoms.entity.UserEntity;
import com.company.itoms.entity.WorkOrderEntity;
import com.company.itoms.entity.WorkOrderFlowLogEntity;
import com.company.itoms.event.UserDeleteEvent;
import com.company.itoms.event.UserStatusChangeEvent;
import com.company.itoms.service.WorkOrderService;
import com.company.itoms.mapper.WorkOrderFlowLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserChangeEventListener {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(UserChangeEventListener.class);
    private final WorkOrderService workOrderService;
    private final WorkOrderFlowLogMapper flowLogMapper;

    @Async
    @EventListener
    public void handleUserStatusChange(UserStatusChangeEvent event) {
        try {
            UserEntity oldUser = event.getOldUser();
            UserEntity newUser = event.getNewUser();

            boolean deptChanged = oldUser.getDepartmentId() != null && !oldUser.getDepartmentId().equals(newUser.getDepartmentId());
            boolean statusDisabled = newUser.getStatus() != null && newUser.getStatus() == 0;
            boolean isDeleted = newUser.getIsDeleted() != null && newUser.getIsDeleted() == 1;

            if (deptChanged || statusDisabled || isDeleted) {
                reassignUserWorkOrders(newUser.getId(), "人员异动(部门变更或禁用)，系统自动回收派单");
            }
        } catch (Exception e) {
            log.error("处理用户状态变更联动派单异常，降级处理：", e);
        }
    }

    @Async
    @EventListener
    public void handleUserDelete(UserDeleteEvent event) {
        try {
            reassignUserWorkOrders(event.getUserId(), "人员被删除，系统自动回收派单");
        } catch (Exception e) {
            log.error("处理用户删除联动派单异常，降级处理：", e);
        }
    }

    private void reassignUserWorkOrders(Long userId, String reason) {
        LambdaQueryWrapper<WorkOrderEntity> query = new LambdaQueryWrapper<>();
        query.eq(WorkOrderEntity::getAssigneeId, userId)
             .in(WorkOrderEntity::getStatus, WorkOrderStatusEnum.PENDING.getCode(), WorkOrderStatusEnum.PROCESSING.getCode());
        
        List<WorkOrderEntity> workOrders = workOrderService.list(query);
        for (WorkOrderEntity wo : workOrders) {
            try {
                wo.setAssigneeId(null);
                wo.setStatus(WorkOrderStatusEnum.PENDING.getCode());
                workOrderService.updateById(wo);

                WorkOrderFlowLogEntity logEntity = new WorkOrderFlowLogEntity();
                logEntity.setWorkOrderId(wo.getId());
                logEntity.setOperatorId(0L); // System
                logEntity.setAction("SYSTEM_REASSIGN");
                logEntity.setPreviousStatus(wo.getStatus());
                logEntity.setCurrentStatus(WorkOrderStatusEnum.PENDING.getCode());
                logEntity.setRemark(reason);
                logEntity.setCreateTime(LocalDateTime.now());
                flowLogMapper.insert(logEntity);

                try {
                    workOrderService.autoAssignWorkOrder(wo.getId());
                } catch (Exception e) {
                    log.warn("工单 {} 重新自动派单失败，将投入公共抢单池：{}", wo.getId(), e.getMessage());
                }
            } catch (Exception e) {
                log.error("工单 [{}] 回收派单异常，跳过：{}", wo.getId(), e.getMessage());
            }
        }
    }
}
