package com.company.itoms.aspect;

import com.company.itoms.service.DashboardService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class DashboardSyncAspect {

    @Autowired
    private DashboardService dashboardService;

    @Async
    @AfterReturning(
        pointcut = "execution(* com.company.itoms.service.impl.*.save*(..)) || " +
                   "execution(* com.company.itoms.service.impl.*.update*(..)) || " +
                   "execution(* com.company.itoms.service.impl.*.remove*(..)) || " +
                   "execution(* com.company.itoms.service.impl.*.delete*(..)) || " +
                   "execution(* com.company.itoms.service.impl.*.finish*(..)) || " +
                   "execution(* com.company.itoms.service.impl.*.evaluate*(..))",
        returning = "result"
    )
    public void afterCudOperations(Object result) {
        // 为了避免每次查询操作都触发，简单降级：如果是返回boolean或void的方法通常是CUD
        // 这里做增量/全量同步
        try {
            dashboardService.syncMetrics();
        } catch (Exception e) {
            log.error("AOP 触发大屏数据同步异常，降级处理：", e);
        }
    }
}
