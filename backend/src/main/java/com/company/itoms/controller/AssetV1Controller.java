package com.company.itoms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.itoms.common.Result;
import com.company.itoms.entity.AssetEntity;
import com.company.itoms.entity.AssetLifecycleLogEntity;
import com.company.itoms.service.AssetService;
import com.company.itoms.service.AssetLifecycleLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/assets")
public class AssetV1Controller {
    private static final Logger logger = LoggerFactory.getLogger(AssetV1Controller.class);
    @Autowired
    private AssetService assetService;

    @Autowired
    private AssetLifecycleLogService assetLifecycleLogService;

    @GetMapping
    public Result<Map<String, Object>> getAssets(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String department) {

        logger.info("Getting assets with page: {}, pageSize: {}, name: {}, type: {}, status: {}, department: {}", page, pageSize, name, type, status, department);

        try {
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AssetEntity> queryWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();

            queryWrapper.eq(AssetEntity::getIsDeleted, 0);

            if (name != null && !name.isEmpty()) {
                queryWrapper.like(AssetEntity::getAssetName, name);
            }
            if (type != null && !type.isEmpty()) {
                queryWrapper.eq(AssetEntity::getAssetType, type);
            }
            if (status != null && !status.isEmpty()) {
                queryWrapper.eq(AssetEntity::getAssetStatus, status);
            }
            if (department != null && !department.isEmpty()) {
                queryWrapper.eq(AssetEntity::getDepartment, department);
            }

            logger.info("Query wrapper created, executing list query...");
            List<AssetEntity> assets = assetService.list(queryWrapper);
            logger.info("List query executed successfully, total: {}", assets.size());

            int start = (page - 1) * pageSize;
            int end = Math.min(start + pageSize, assets.size());
            List<AssetEntity> pagedAssets = start < assets.size() ? assets.subList(start, end) : new java.util.ArrayList<>();

            Map<String, Object> response = new HashMap<>();
            response.put("total", assets.size());
            response.put("page", page);
            response.put("pageSize", pageSize);
            response.put("list", pagedAssets.stream().map(asset -> transformAsset(asset)).collect(Collectors.toList()));

            logger.info("Response created, returning success");
            return Result.success(response);
        } catch (Exception e) {
            logger.error("Error getting assets", e);
            return Result.error(500, "服务暂时不可用，请稍后重试");
        }
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        return Result.success(assetService.getAssetStatistics());
    }

    @GetMapping("/status/{status}")
    public Result<List<AssetEntity>> getAssetsByStatus(@PathVariable String status) {
        return Result.success(assetService.getAssetsByStatus(status));
    }

    @GetMapping("/department/{department}")
    public Result<List<AssetEntity>> getAssetsByDepartment(@PathVariable String department) {
        return Result.success(assetService.getAssetsByDepartment(department));
    }

    @GetMapping("/warranty-expiring")
    public Result<List<AssetEntity>> getWarrantyExpiringAssets(@RequestParam(defaultValue = "30") int days) {
        return Result.success(assetService.getWarrantyExpiringAssets(days));
    }

    @GetMapping("/idle")
    public Result<List<AssetEntity>> getIdleAssets() {
        return Result.success(assetService.getAssetsByStatus("IDLE"));
    }

    @PostMapping
    public Result<AssetEntity> createAsset(@RequestBody Map<String, Object> data) {
        AssetEntity asset = new AssetEntity();
        asset.setAssetName((String) data.get("name"));
        asset.setBrand((String) data.get("brand"));
        asset.setConfig((String) data.get("config"));
        asset.setDepartment((String) data.get("department"));
        asset.setUserName((String) data.get("user"));
        asset.setAssetType((String) data.get("type"));
        asset.setAssetSubtype((String) data.get("subtype"));
        asset.setIp((String) data.get("ip"));
        asset.setLocation((String) data.get("location"));
        asset.setAssetStatus((String) data.get("assetStatus"));
        asset.setSnCode((String) data.get("snCode"));
        asset.setManagerName((String) data.get("managerName"));
        asset.setMaintenanceContact((String) data.get("maintenanceContact"));
        asset.setRemark((String) data.get("remark"));

        if (data.get("purchaseDate") != null) {
            try {
                asset.setPurchaseDate(java.time.LocalDate.parse((String) data.get("purchaseDate")));
            } catch (Exception e) {
                asset.setPurchaseDate(java.time.LocalDate.now());
            }
        }

        if (data.get("purchasePrice") != null) {
            try {
                asset.setPurchasePrice(new java.math.BigDecimal(data.get("purchasePrice").toString()));
            } catch (Exception e) {
                asset.setPurchasePrice(java.math.BigDecimal.ZERO);
            }
        }

        if (data.get("warrantyExpireDate") != null) {
            try {
                asset.setWarrantyExpireDate(java.time.LocalDate.parse((String) data.get("warrantyExpireDate")));
            } catch (Exception e) {
            }
        }

        if (data.get("totalLife") != null) {
            try {
                asset.setTotalLife(Double.parseDouble(data.get("totalLife").toString()));
            } catch (Exception e) {
                asset.setTotalLife(10.0);
            }
        }

        asset.setAssetCode("ASSET" + System.currentTimeMillis());
        asset.setAge(0.0);
        asset.setRepairCount(0);
        asset.setIsDeleted(0);
        if (asset.getAssetStatus() == null) {
            asset.setAssetStatus("IN_USE");
        }
        assetService.save(asset);

        assetLifecycleLogService.recordLog(asset.getId(), "CREATE", 1L, "system", "创建资产");

        return Result.success(asset);
    }

    @PutMapping("/{id}")
    public Result<AssetEntity> updateAsset(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        AssetEntity asset = assetService.getById(id);
        if (asset != null) {
            if (data.containsKey("name")) asset.setAssetName((String) data.get("name"));
            if (data.containsKey("brand")) asset.setBrand((String) data.get("brand"));
            if (data.containsKey("config")) asset.setConfig((String) data.get("config"));
            if (data.containsKey("department")) asset.setDepartment((String) data.get("department"));
            if (data.containsKey("user")) asset.setUserName((String) data.get("user"));
            if (data.containsKey("type")) asset.setAssetType((String) data.get("type"));
            if (data.containsKey("subtype")) asset.setAssetSubtype((String) data.get("subtype"));
            if (data.containsKey("ip")) asset.setIp((String) data.get("ip"));
            if (data.containsKey("location")) asset.setLocation((String) data.get("location"));
            if (data.containsKey("assetStatus")) asset.setAssetStatus((String) data.get("assetStatus"));
            if (data.containsKey("snCode")) asset.setSnCode((String) data.get("snCode"));
            if (data.containsKey("managerName")) asset.setManagerName((String) data.get("managerName"));
            if (data.containsKey("maintenanceContact")) asset.setMaintenanceContact((String) data.get("maintenanceContact"));
            if (data.containsKey("remark")) asset.setRemark((String) data.get("remark"));

            if (data.containsKey("purchaseDate")) {
                try {
                    asset.setPurchaseDate(java.time.LocalDate.parse((String) data.get("purchaseDate")));
                } catch (Exception e) {}
            }
            if (data.containsKey("purchasePrice")) {
                try {
                    asset.setPurchasePrice(new java.math.BigDecimal(data.get("purchasePrice").toString()));
                } catch (Exception e) {}
            }
            if (data.containsKey("warrantyExpireDate")) {
                try {
                    asset.setWarrantyExpireDate(java.time.LocalDate.parse((String) data.get("warrantyExpireDate")));
                } catch (Exception e) {}
            }

            assetService.updateById(asset);
            assetLifecycleLogService.recordLog(id, "UPDATE", 1L, "system", "更新资产信息");
        }
        return Result.success(asset);
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteAsset(@PathVariable Long id) {
        return Result.success(assetService.removeById(id));
    }

    @PostMapping("/{id}/receive")
    public Result<Void> receiveAsset(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        assetService.receiveAsset(
            id,
            ((Number) data.get("operatorId")).longValue(),
            (String) data.get("operatorName"),
            (String) data.get("targetDepartment"),
            (String) data.get("targetUser")
        );
        return Result.success(null);
    }

    @PostMapping("/{id}/transfer")
    public Result<Void> transferAsset(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        assetService.transferAsset(
            id,
            ((Number) data.get("operatorId")).longValue(),
            (String) data.get("operatorName"),
            (String) data.get("targetDepartment"),
            (String) data.get("targetUser"),
            (String) data.get("reason")
        );
        return Result.success(null);
    }

    @PostMapping("/{id}/return")
    public Result<Void> returnAsset(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        assetService.returnAsset(
            id,
            ((Number) data.get("operatorId")).longValue(),
            (String) data.get("operatorName"),
            (String) data.get("reason")
        );
        return Result.success(null);
    }

    @PostMapping("/{id}/repair")
    public Result<Void> repairAsset(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        assetService.repairAsset(
            id,
            ((Number) data.get("operatorId")).longValue(),
            (String) data.get("operatorName"),
            (String) data.get("reason")
        );
        return Result.success(null);
    }

    @PostMapping("/{id}/scrap")
    public Result<Void> scrapAsset(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        assetService.scrapAsset(
            id,
            ((Number) data.get("operatorId")).longValue(),
            (String) data.get("operatorName"),
            (String) data.get("reason")
        );
        return Result.success(null);
    }

    @PostMapping("/{id}/create-workorder")
    public Result<Map<String, Object>> createWorkOrderFromAsset(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        Long workOrderId = assetService.createWorkOrderFromAsset(
            id,
            ((Number) data.get("operatorId")).longValue(),
            (String) data.get("description"),
            data.get("urgencyLevel") != null ? ((Number) data.get("urgencyLevel")).intValue() : 2
        );

        Map<String, Object> result = new HashMap<>();
        result.put("workOrderId", workOrderId);
        return Result.success(result);
    }

    @GetMapping("/{id}/history")
    public Result<List<AssetLifecycleLogEntity>> getAssetHistory(@PathVariable Long id) {
        return Result.success(assetLifecycleLogService.getLogsByAssetId(id));
    }

    @PostMapping("/batch-assign")
    public Result<Boolean> batchAssign(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) request.get("ids");
        String targetDepartment = (String) request.get("department");
        String targetUser = (String) request.get("user");
        Long operatorId = ((Number) request.get("operatorId")).longValue();

        if (ids != null && !ids.isEmpty()) {
            for (Long id : ids) {
                assetService.receiveAsset(id, operatorId, "system", targetDepartment, targetUser);
            }
        }
        return Result.success(true);
    }

    @PostMapping("/batch-close")
    public Result<Boolean> batchClose(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) request.get("ids");
        Long operatorId = ((Number) request.get("operatorId")).longValue();

        if (ids != null && !ids.isEmpty()) {
            for (Long id : ids) {
                assetService.scrapAsset(id, operatorId, "system", "批量报废");
            }
        }
        return Result.success(true);
    }

    private Map<String, Object> transformAsset(AssetEntity asset) {
        Map<String, Object> map = new HashMap<>();
        if (asset == null) {
            return map;
        }
        map.put("id", asset.getId());
        map.put("assetCode", asset.getAssetCode());
        map.put("snCode", asset.getSnCode());
        map.put("name", asset.getAssetName());
        map.put("brand", asset.getBrand());
        map.put("config", asset.getConfig());
        map.put("department", asset.getDepartment());
        map.put("user", asset.getUserName());
        map.put("type", asset.getAssetType() != null ? asset.getAssetType() : "server");
        map.put("subtype", asset.getAssetSubtype());
        map.put("ip", asset.getIp() != null ? asset.getIp() : "");
        map.put("location", asset.getLocation());
        map.put("assetStatus", asset.getAssetStatus() != null ? asset.getAssetStatus() : "IN_USE");
        map.put("purchaseDate", asset.getPurchaseDate() != null ? asset.getPurchaseDate().toString() : "");
        map.put("purchasePrice", asset.getPurchasePrice() != null ? asset.getPurchasePrice().toString() : "");
        map.put("warrantyExpireDate", asset.getWarrantyExpireDate() != null ? asset.getWarrantyExpireDate().toString() : "");
        map.put("managerName", asset.getManagerName());
        map.put("maintenanceContact", asset.getMaintenanceContact());
        map.put("remark", asset.getRemark());
        map.put("totalLife", asset.getTotalLife());
        map.put("age", asset.getAge());
        map.put("repairCount", asset.getRepairCount());
        map.put("status", asset.getIsDeleted() != null && asset.getIsDeleted() == 1 ? "deleted" : "active");
        return map;
    }
}