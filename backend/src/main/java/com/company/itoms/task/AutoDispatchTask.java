package com.company.itoms.task;

import com.company.itoms.service.WorkOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AutoDispatchTask {

    @Autowired
    private WorkOrderService workOrderService;

    /**
     * 自动派单定时任务
     * 每1分钟执行一次
     */
    @Scheduled(fixedRate = 60000)
    public void autoDispatch() {
        try {
            log.info("开始执行自动派单任务");
            boolean success = workOrderService.autoDispatchWorkOrders();
            log.info("自动派单任务执行完成，结果: {}", success);
        } catch (Exception e) {
            log.error("自动派单任务执行失败", e);
        }
    }

    /**
     * 生成工程师工作量统计
     * 每天凌晨1点执行
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void generateEngineerWorkStats() {
        try {
            log.info("开始生成工程师工作量统计");
            boolean success = workOrderService.generateEngineerWorkStats(java.time.LocalDate.now().minusDays(1));
            log.info("工程师工作量统计生成完成，结果: {}", success);
        } catch (Exception e) {
            log.error("生成工程师工作量统计失败", e);
        }
    }
}
