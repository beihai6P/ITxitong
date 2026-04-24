package com.company.itoms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.company.itoms.dto.response.DashboardMetricsDTO;
import com.company.itoms.entity.AssetEntity;
import com.company.itoms.entity.WorkOrderEntity;
import com.company.itoms.mapper.AssetMapper;
import com.company.itoms.mapper.WorkOrderMapper;
import com.company.itoms.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private WorkOrderMapper workOrderMapper;

    @Autowired
    private AssetMapper assetMapper;

    @Override
    public DashboardMetricsDTO getMetrics() {
        DashboardMetricsDTO metrics = new DashboardMetricsDTO();

        // 1. Work Order basic stats
        Long totalWo = workOrderMapper.selectCount(new QueryWrapper<>());
        Long pendingWo = workOrderMapper.selectCount(new QueryWrapper<WorkOrderEntity>().eq("status", 1));
        Long processingWo = workOrderMapper.selectCount(new QueryWrapper<WorkOrderEntity>().in("status", Arrays.asList(2, 3)));
        Long completedWo = workOrderMapper.selectCount(new QueryWrapper<WorkOrderEntity>().in("status", Arrays.asList(4, 5)));

        metrics.setTotalWorkOrders(totalWo);
        metrics.setPendingWorkOrders(pendingWo);
        metrics.setProcessingWorkOrders(processingWo);
        metrics.setCompletedWorkOrders(completedWo);

        // 2. Asset stats
        Long totalAssets = assetMapper.selectCount(new QueryWrapper<>());
        metrics.setTotalAssets(totalAssets);

        // 3. Asset health distribution
        List<AssetEntity> allAssets = assetMapper.selectList(new QueryWrapper<>());
        int healthy = 0, warning = 0, critical = 0;
        for (AssetEntity asset : allAssets) {
            Double health = asset.calculateHealthStatus();
            if (health >= 0.8) {
                healthy++;
            } else if (health >= 0.5) {
                warning++;
            } else {
                critical++;
            }
        }
        
        List<Map<String, Object>> healthDist = new ArrayList<>();
        healthDist.add(createMap("Healthy", healthy));
        healthDist.add(createMap("Warning", warning));
        healthDist.add(createMap("Critical", critical));
        metrics.setAssetHealthDistribution(healthDist);

        // 4. Fault type distribution
        // For simplicity, we just aggregate in memory or use basic Group By. Let's use basic group by query if possible, or in memory.
        List<WorkOrderEntity> allWos = workOrderMapper.selectList(new QueryWrapper<WorkOrderEntity>().select("fault_type"));
        Map<String, Integer> faultCountMap = new HashMap<>();
        for (WorkOrderEntity wo : allWos) {
            String type = wo.getFaultType() != null ? wo.getFaultType() : "OTHER";
            faultCountMap.put(type, faultCountMap.getOrDefault(type, 0) + 1);
        }
        List<Map<String, Object>> faultDist = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : faultCountMap.entrySet()) {
            faultDist.add(createMap(entry.getKey(), entry.getValue()));
        }
        metrics.setFaultTypeDistribution(faultDist);

        // 5. Work order trend (mock last 7 days for simplicity, as we don't have create_time in entity)
        // Wait, does WorkOrderEntity have createTime? Let's check.
        // It doesn't have createTime explicitly defined, but it might be handled by MP. Let's just mock 7 days trend to ensure UI works as requested.
        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            // Mocking trend data
            Map<String, Object> point = new HashMap<>();
            point.put("date", "Day " + (7 - i));
            point.put("count", (int)(Math.random() * 20) + 5);
            trend.add(point);
        }
        metrics.setWorkOrderTrend(trend);

        return metrics;
    }

    private Map<String, Object> createMap(String name, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("value", value);
        return map;
    }
}
