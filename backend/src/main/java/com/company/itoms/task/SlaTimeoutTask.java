package com.company.itoms.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.itoms.common.WebSocketServer;
import com.company.itoms.entity.WorkOrderEntity;
import com.company.itoms.entity.WorkOrderFlowLogEntity;
import com.company.itoms.enums.UrgencyLevelEnum;
import com.company.itoms.mapper.WorkOrderFlowLogMapper;
import com.company.itoms.mapper.WorkOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SlaTimeoutTask {

    private final WorkOrderMapper workOrderMapper;
    private final WorkOrderFlowLogMapper workOrderFlowLogMapper;
    private final WebSocketServer webSocketServer;

    @Scheduled(cron = "0 * * * * ?") // 每分钟执行一次
    @Transactional(rollbackFor = Exception.class)
    public void checkSlaTimeout() {
        log.info("开始检查SLA超时的工单...");

        LambdaQueryWrapper<WorkOrderEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(WorkOrderEntity::getStatus, 1, 2) // 待处理，处理中
                .lt(WorkOrderEntity::getSlaDeadline, LocalDateTime.now())
                .ne(WorkOrderEntity::getUrgencyLevel, UrgencyLevelEnum.EXTREME_URGENCY.code()); // 排除已经设置为特急的

        List<WorkOrderEntity> timeoutOrders = workOrderMapper.selectList(wrapper);

        if (timeoutOrders == null || timeoutOrders.isEmpty()) {
            log.info("没有发现SLA超时的工单。");
            return;
        }

        for (WorkOrderEntity order : timeoutOrders) {
            log.info("工单 {} 已超过SLA截止时间，进行升级处理", order.getWorkOrderCode());

            Integer oldStatus = order.getStatus();

            // 更新工单优先级和紧急程度
            order.setUrgencyLevel(UrgencyLevelEnum.EXTREME_URGENCY.code());
            order.setPriorityLevel(999); // 设置最高优先级
            order.setUpdateTime(LocalDateTime.now());

            workOrderMapper.updateById(order);

            // 记录流转日志
            WorkOrderFlowLogEntity flowLog = new WorkOrderFlowLogEntity();
            flowLog.setWorkOrderId(order.getId());
            flowLog.setOperatorId(0L); // 系统操作
            flowLog.setAction("SLA_TIMEOUT");
            flowLog.setPreviousStatus(oldStatus);
            flowLog.setCurrentStatus(order.getStatus());
            flowLog.setRemark("工单超过SLA截止时间，系统自动提升为特急");
            workOrderFlowLogMapper.insert(flowLog);

            // 推送WebSocket通知
            String message = String.format("{\"type\":\"SLA_TIMEOUT\",\"workOrderCode\":\"%s\",\"message\":\"工单 %s 已超过SLA期限，请尽快处理！\"}",
                    order.getWorkOrderCode(), order.getWorkOrderCode());

            if (order.getAssigneeId() != null) {
                // 通知维修人
                webSocketServer.sendMessage(String.valueOf(order.getAssigneeId()), message);
            } else {
                // 如果没有分配，广播给所有人或管理员（这里选择广播）
                webSocketServer.broadcast(message);
            }
        }

        log.info("SLA超时检查完成，共处理了 {} 个工单。", timeoutOrders.size());
    }



}
