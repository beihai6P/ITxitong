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
    
    // {"date": "2023-10-01", "count": 10}
    private List<Map<String, Object>> workOrderTrend;
    
    // {"name": "HARDWARE", "value": 20}
    private List<Map<String, Object>> faultTypeDistribution;
    
    // {"name": "Healthy", "value": 50}
    private List<Map<String, Object>> assetHealthDistribution;
}
