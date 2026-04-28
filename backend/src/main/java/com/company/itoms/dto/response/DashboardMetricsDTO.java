package com.company.itoms.dto.response;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DashboardMetricsDTO {
    private Long totalWorkOrders;
    private Long pendingWorkOrders;
    private Long processingWorkOrders;
    private Long completedWorkOrders;
    private Long totalAssets;
    private Long totalUsers;
    private Double completionRate;
    private Long aiPredictedFaults;
    private Double aiSuccessRate;

    private List<Map<String, Object>> workOrderTrend;
    private List<Map<String, Object>> faultTypeDistribution;
    private List<Map<String, Object>> assetHealthDistribution;
    private List<Map<String, Object>> departmentWorkOrders;
    private List<Map<String, Object>> assetTypeDistribution;
    private List<Map<String, Object>> aiPredictionTrend;
    private List<List<Integer>> faultTimeDistribution;
    private List<Map<String, Object>> responseTimeRanking;
}
