package com.company.itoms.service;

import com.company.itoms.dto.response.DashboardMetricsDTO;

public interface DashboardService {
    DashboardMetricsDTO getMetrics();
    void syncMetrics();
}
