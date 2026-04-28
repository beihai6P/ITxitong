package com.company.itoms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.company.itoms.dto.response.DashboardMetricsDTO;
import com.company.itoms.entity.AssetEntity;
import com.company.itoms.entity.DepartmentEntity;
import com.company.itoms.entity.WorkOrderEntity;
import com.company.itoms.mapper.AssetMapper;
import com.company.itoms.mapper.DepartmentMapper;
import com.company.itoms.mapper.UserMapper;
import com.company.itoms.mapper.WorkOrderMapper;
import com.company.itoms.service.DashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.data.redis.core.RedisTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private WorkOrderMapper workOrderMapper;

    @Autowired
    private AssetMapper assetMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String DASHBOARD_CACHE_KEY = "itoms:dashboard:metrics";

    @Override
    public DashboardMetricsDTO getMetrics() {
        try {
            String cached = redisTemplate.opsForValue().get(DASHBOARD_CACHE_KEY);
            if (cached != null) {
                return objectMapper.readValue(cached, DashboardMetricsDTO.class);
            }
        } catch (Exception e) {
            log.error("读取大屏 Redis 缓存失败，降级查询底层库：", e);
        }
        
        // 降级查询
        return calculateMetrics();
    }

    @Override
    public void syncMetrics() {
        try {
            DashboardMetricsDTO metrics = calculateMetrics();
            String json = objectMapper.writeValueAsString(metrics);
            redisTemplate.opsForValue().set(DASHBOARD_CACHE_KEY, json, 1, TimeUnit.HOURS);
            log.info("大屏指标数据重算并同步至 Redis 成功");
        } catch (Exception e) {
            log.error("大屏指标数据同步失败：", e);
        }
    }

    private DashboardMetricsDTO calculateMetrics() {
        DashboardMetricsDTO metrics = new DashboardMetricsDTO();

        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime sevenDaysAgo = now.minusDays(7);

            // 1. Work Order basic stats - query all and count in memory
            QueryWrapper<WorkOrderEntity> woQuery = new QueryWrapper<>();
            woQuery.eq("is_deleted", 0);
            List<WorkOrderEntity> allWorkOrders = safeQuery(() -> workOrderMapper.selectList(woQuery), Collections.emptyList());
            List<WorkOrderEntity> pendingOrders = filterByStatus(allWorkOrders, 1);
            List<WorkOrderEntity> processingOrders = filterByStatusIn(allWorkOrders, Arrays.asList(2, 3));
            List<WorkOrderEntity> completedOrders = filterByStatusIn(allWorkOrders, Arrays.asList(4, 5));

            metrics.setTotalWorkOrders((long) allWorkOrders.size());
            metrics.setPendingWorkOrders((long) pendingOrders.size());
            metrics.setProcessingWorkOrders((long) processingOrders.size());
            metrics.setCompletedWorkOrders((long) completedOrders.size());

            // 2. Asset stats
            QueryWrapper<AssetEntity> assetQuery = new QueryWrapper<>();
            assetQuery.eq("is_deleted", 0);
            List<AssetEntity> allAssets = safeQuery(() -> assetMapper.selectList(assetQuery), Collections.emptyList());
            metrics.setTotalAssets((long) allAssets.size());

            // 3. User stats
            QueryWrapper<UserEntity> userQuery = new QueryWrapper<>();
            userQuery.eq("is_deleted", 0);
            Long totalUsers = safeQuery(() -> userMapper.selectCount(userQuery), 0L);
            metrics.setTotalUsers(totalUsers);

            // 4. Completion rate
            double completionRate = allWorkOrders.size() > 0
                ? (double) completedOrders.size() / allWorkOrders.size() * 100 : 0;
            metrics.setCompletionRate(Math.round(completionRate * 10) / 10.0);

            // 5. Asset health distribution
            int healthy = 0, warning = 0, critical = 0;
            for (AssetEntity asset : allAssets) {
                Double health = safeCall(() -> asset.calculateHealthStatus(), 1.0);
                if (health >= 0.8) {
                    healthy++;
                } else if (health >= 0.5) {
                    warning++;
                } else {
                    critical++;
                }
            }

            List<Map<String, Object>> healthDist = new ArrayList<>();
            healthDist.add(createMap("良好", healthy));
            healthDist.add(createMap("一般", warning));
            healthDist.add(createMap("较差", critical));
            metrics.setAssetHealthDistribution(healthDist);

            // 6. Fault type distribution
            Map<String, Integer> faultCountMap = new HashMap<>();
            for (WorkOrderEntity wo : allWorkOrders) {
                String type = wo.getFaultType() != null ? wo.getFaultType() : "其他";
                faultCountMap.put(type, faultCountMap.getOrDefault(type, 0) + 1);
            }
            List<Map<String, Object>> faultDist = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : faultCountMap.entrySet()) {
                faultDist.add(createMap(entry.getKey(), entry.getValue()));
            }
            metrics.setFaultTypeDistribution(faultDist);

            // 7. Work order trend (last 7 days)
            DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("MM-dd");
            Map<String, Long> dailyCountMap = new LinkedHashMap<>();
            for (int i = 6; i >= 0; i--) {
                String day = now.minusDays(i).format(dayFormatter);
                dailyCountMap.put(day, 0L);
            }
            for (WorkOrderEntity wo : allWorkOrders) {
                if (wo.getCreateTime() != null) {
                    String day = wo.getCreateTime().format(dayFormatter);
                    if (dailyCountMap.containsKey(day)) {
                        dailyCountMap.merge(day, 1L, Long::sum);
                    }
                }
            }
            List<Map<String, Object>> trend = new ArrayList<>();
            for (Map.Entry<String, Long> entry : dailyCountMap.entrySet()) {
                Map<String, Object> point = new HashMap<>();
                point.put("date", entry.getKey());
                point.put("count", entry.getValue());
                trend.add(point);
            }
            metrics.setWorkOrderTrend(trend);

            // 8. Department work order distribution
            List<DepartmentEntity> departments = safeQuery(() -> departmentMapper.selectList(new QueryWrapper<>()), Collections.emptyList());
            Map<Long, String> deptIdToName = new HashMap<>();
            for (DepartmentEntity dept : departments) {
                if (dept.getId() != null && dept.getName() != null) {
                    deptIdToName.put(dept.getId(), dept.getName());
                }
            }
            Map<Long, Integer> deptWoCountMap = new HashMap<>();
            for (WorkOrderEntity wo : allWorkOrders) {
                if (wo.getDepartmentId() != null) {
                    deptWoCountMap.merge(wo.getDepartmentId(), 1, Integer::sum);
                }
            }
            List<Map<String, Object>> deptDist = new ArrayList<>();
            for (Map.Entry<Long, Integer> entry : deptWoCountMap.entrySet()) {
                String deptName = deptIdToName.getOrDefault(entry.getKey(), "未知部门");
                deptDist.add(createMap(deptName, entry.getValue()));
            }
            metrics.setDepartmentWorkOrders(deptDist);

            // 9. Asset type distribution
            Map<String, Integer> assetTypeCountMap = new HashMap<>();
            for (AssetEntity asset : allAssets) {
                String type = asset.getAssetType() != null ? asset.getAssetType() : "其他";
                assetTypeCountMap.put(type, assetTypeCountMap.getOrDefault(type, 0) + 1);
            }
            List<Map<String, Object>> assetTypeDist = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : assetTypeCountMap.entrySet()) {
                assetTypeDist.add(createMap(entry.getKey(), entry.getValue()));
            }
            metrics.setAssetTypeDistribution(assetTypeDist);

            // 10. AI metrics (simulated based on completed work orders)
            int aiPredictedFaults = (int) (completedOrders.size() * 0.05);
            metrics.setAiPredictedFaults((long) aiPredictedFaults);
            double aiSuccessRate = completedOrders.size() > 0 ? 75.0 + Math.random() * 20 : 0;
            metrics.setAiSuccessRate(Math.round(aiSuccessRate * 10) / 10.0);

            // 11. AI prediction trend (simulated)
            List<Map<String, Object>> aiTrend = new ArrayList<>();
            for (int i = 6; i >= 0; i--) {
                Map<String, Object> point = new HashMap<>();
                point.put("date", now.minusDays(i).format(dayFormatter));
                point.put("count", (int) (Math.random() * 10) + 3);
                aiTrend.add(point);
            }
            metrics.setAiPredictionTrend(aiTrend);

            // 12. Fault time distribution (simulated heatmap data)
            List<List<Integer>> faultTimeDist = new ArrayList<>();
            for (int day = 0; day < 7; day++) {
                for (int hour = 0; hour < 12; hour++) {
                    List<Integer> point = new ArrayList<>();
                    point.add(day);
                    point.add(hour);
                    int count = 0;
                    if (hour >= 9 && hour <= 11) {
                        count = (int) (Math.random() * 15) + 5;
                    } else if (hour >= 14 && hour <= 17) {
                        count = (int) (Math.random() * 12) + 3;
                    }
                    point.add(count);
                    faultTimeDist.add(point);
                }
            }
            metrics.setFaultTimeDistribution(faultTimeDist);

            // 13. Response time ranking (simulated by department)
            List<Map<String, Object>> responseRanking = new ArrayList<>();
            for (Map.Entry<Long, Integer> entry : deptWoCountMap.entrySet()) {
                String deptName = deptIdToName.getOrDefault(entry.getKey(), "未知部门");
                Map<String, Object> item = new HashMap<>();
                item.put("name", deptName);
                item.put("value", (int) (Math.random() * 30) + 10);
                responseRanking.add(item);
            }
            metrics.setResponseTimeRanking(responseRanking);

        } catch (Exception e) {
            log.error("Dashboard metrics calculation failed", e);
            // 返回默认空数据，不抛出异常
            return createEmptyMetrics();
        }

        return metrics;
    }

    private <T> List<T> safeQuery(java.util.function.Supplier<List<T>> query, List<T> defaultValue) {
        try {
            return query.get();
        } catch (Exception e) {
            log.warn("Query failed, returning default value", e);
            return defaultValue;
        }
    }

    private <T> T safeQuery(java.util.function.Supplier<T> query, T defaultValue) {
        try {
            return query.get();
        } catch (Exception e) {
            log.warn("Query failed, returning default value", e);
            return defaultValue;
        }
    }

    private <T> T safeCall(java.util.function.Supplier<T> call, T defaultValue) {
        try {
            return call.get();
        } catch (Exception e) {
            log.warn("Call failed, returning default value", e);
            return defaultValue;
        }
    }

    private List<WorkOrderEntity> filterByStatus(List<WorkOrderEntity> list, Integer status) {
        return list.stream()
            .filter(wo -> wo.getStatus() != null && wo.getStatus().equals(status))
            .collect(Collectors.toList());
    }

    private List<WorkOrderEntity> filterByStatusIn(List<WorkOrderEntity> list, List<Integer> statuses) {
        return list.stream()
            .filter(wo -> wo.getStatus() != null && statuses.contains(wo.getStatus()))
            .collect(Collectors.toList());
    }

    private Map<String, Object> createMap(String name, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("value", value);
        return map;
    }

    private DashboardMetricsDTO createEmptyMetrics() {
        DashboardMetricsDTO metrics = new DashboardMetricsDTO();
        metrics.setTotalWorkOrders(0L);
        metrics.setPendingWorkOrders(0L);
        metrics.setProcessingWorkOrders(0L);
        metrics.setCompletedWorkOrders(0L);
        metrics.setTotalAssets(0L);
        metrics.setTotalUsers(0L);
        metrics.setCompletionRate(0.0);
        metrics.setAiPredictedFaults(0L);
        metrics.setAiSuccessRate(0.0);
        metrics.setWorkOrderTrend(new ArrayList<>());
        metrics.setAssetHealthDistribution(new ArrayList<>());
        metrics.setFaultTypeDistribution(new ArrayList<>());
        metrics.setDepartmentWorkOrders(new ArrayList<>());
        metrics.setAssetTypeDistribution(new ArrayList<>());
        metrics.setAiPredictionTrend(new ArrayList<>());
        metrics.setFaultTimeDistribution(new ArrayList<>());
        metrics.setResponseTimeRanking(new ArrayList<>());
        return metrics;
    }
}
