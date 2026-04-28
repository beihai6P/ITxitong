package com.company.itoms.controller;

import com.company.itoms.common.Result;
import com.company.itoms.dto.response.DashboardMetricsDTO;
import com.company.itoms.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/metrics")
    public Result<DashboardMetricsDTO> getMetrics() {
        DashboardMetricsDTO metrics = dashboardService.getMetrics();
        return Result.success(metrics);
    }

    @GetMapping("/sync")
    public Result<Boolean> syncMetrics() {
        dashboardService.syncMetrics();
        return Result.success(true);
    }
}
