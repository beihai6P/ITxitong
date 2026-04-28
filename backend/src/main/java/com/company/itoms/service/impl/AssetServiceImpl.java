package com.company.itoms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.itoms.entity.AssetEntity;
import com.company.itoms.entity.AssetLifecycleLogEntity;
import com.company.itoms.exception.BusinessException;
import com.company.itoms.mapper.AssetMapper;
import com.company.itoms.service.AssetService;
import com.company.itoms.service.AssetLifecycleLogService;
import com.company.itoms.dto.response.AssetScanResultDTO;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletResponse;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.company.itoms.dto.request.AssetBatchInventoryDTO;
import com.company.itoms.common.Idempotent;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AssetServiceImpl extends ServiceImpl<AssetMapper, AssetEntity> implements AssetService {

    @Autowired
    private AssetMapper assetMapper;

    @Autowired
    private AssetLifecycleLogService assetLifecycleLogService;

    @Autowired
    private com.company.itoms.service.WorkOrderService workOrderService;

    public static final String STATUS_IN_USE = "IN_USE";
    public static final String STATUS_IDLE = "IDLE";
    public static final String STATUS_REPAIR = "REPAIR";
    public static final String STATUS_SCRAP = "SCRAP";
    public static final String STATUS_PENDING_RETURN = "PENDING_RETURN";

    @Override
    public Double calculateHealth(Long assetId) {
        AssetEntity asset = assetMapper.selectById(assetId);
        if (asset == null) {
            return 0.0;
        }
        return asset.calculateHealthStatus();
    }

    @Override
    public byte[] generateAssetQRCode(Long assetId) {
        AssetEntity asset = getById(assetId);
        if (asset == null) {
            throw new BusinessException(404, "资产不存在");
        }

        try {
            String qrContent = String.format("ASSET:%s,SN:%s,NAME:%s",
                asset.getAssetCode(),
                asset.getSnCode() != null ? asset.getSnCode() : "",
                asset.getAssetName());

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, 200, 200);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new BusinessException(500, "生成二维码失败");
        }
    }

    @Override
    public void exportAssets(HttpServletResponse response) {
        List<AssetEntity> assets = list();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Assets");

            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "资产编码", "SN序列号", "资产名称", "品牌", "型号配置", "资产类型", "资产子类型",
                "所属部门", "使用人", "存放位置", "IP地址", "采购日期", "采购金额", "质保到期日",
                "资产状态", "总寿命", "已用年限", "维修次数", "折旧价值", "管理员", "维保联系人", "备注"};
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            int rowNum = 1;
            for (AssetEntity asset : assets) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(asset.getId() != null ? asset.getId() : 0);
                row.createCell(1).setCellValue(asset.getAssetCode() != null ? asset.getAssetCode() : "");
                row.createCell(2).setCellValue(asset.getSnCode() != null ? asset.getSnCode() : "");
                row.createCell(3).setCellValue(asset.getAssetName() != null ? asset.getAssetName() : "");
                row.createCell(4).setCellValue(asset.getBrand() != null ? asset.getBrand() : "");
                row.createCell(5).setCellValue(asset.getConfig() != null ? asset.getConfig() : "");
                row.createCell(6).setCellValue(asset.getAssetType() != null ? asset.getAssetType() : "");
                row.createCell(7).setCellValue(asset.getAssetSubtype() != null ? asset.getAssetSubtype() : "");
                row.createCell(8).setCellValue(asset.getDepartment() != null ? asset.getDepartment() : "");
                row.createCell(9).setCellValue(asset.getUserName() != null ? asset.getUserName() : "");
                row.createCell(10).setCellValue(asset.getLocation() != null ? asset.getLocation() : "");
                row.createCell(11).setCellValue(asset.getIp() != null ? asset.getIp() : "");
                row.createCell(12).setCellValue(asset.getPurchaseDate() != null ? asset.getPurchaseDate().toString() : "");
                row.createCell(13).setCellValue(asset.getPurchasePrice() != null ? asset.getPurchasePrice().doubleValue() : 0.0);
                row.createCell(14).setCellValue(asset.getWarrantyExpireDate() != null ? asset.getWarrantyExpireDate().toString() : "");
                row.createCell(15).setCellValue(parseAssetStatus(asset.getAssetStatus()));
                row.createCell(16).setCellValue(asset.getTotalLife() != null ? asset.getTotalLife() : 0.0);
                row.createCell(17).setCellValue(asset.getAge() != null ? asset.getAge() : 0.0);
                row.createCell(18).setCellValue(asset.getRepairCount() != null ? asset.getRepairCount() : 0);
                row.createCell(19).setCellValue(asset.getDepreciationValue() != null ? asset.getDepreciationValue().doubleValue() : 0.0);
                row.createCell(20).setCellValue(asset.getManagerName() != null ? asset.getManagerName() : "");
                row.createCell(21).setCellValue(asset.getMaintenanceContact() != null ? asset.getMaintenanceContact() : "");
                row.createCell(22).setCellValue(asset.getRemark() != null ? asset.getRemark() : "");
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("资产列表", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            workbook.write(response.getOutputStream());
        } catch (Exception e) {
            throw new BusinessException(500, "导出资产数据失败");
        }
    }

    @Override
    public void importAssets(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "上传文件为空");
        }

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            List<AssetEntity> assetList = new ArrayList<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                AssetEntity asset = new AssetEntity();
                asset.setAssetCode(getCellValueAsString(row.getCell(1)));
                asset.setSnCode(getCellValueAsString(row.getCell(2)));
                asset.setAssetName(getCellValueAsString(row.getCell(3)));
                asset.setBrand(getCellValueAsString(row.getCell(4)));
                asset.setConfig(getCellValueAsString(row.getCell(5)));
                asset.setAssetType(getCellValueAsString(row.getCell(6)));
                asset.setAssetSubtype(getCellValueAsString(row.getCell(7)));
                asset.setDepartment(getCellValueAsString(row.getCell(8)));
                asset.setUserName(getCellValueAsString(row.getCell(9)));
                asset.setLocation(getCellValueAsString(row.getCell(10)));
                asset.setIp(getCellValueAsString(row.getCell(11)));

                String purchaseDateStr = getCellValueAsString(row.getCell(12));
                if (purchaseDateStr != null && !purchaseDateStr.isEmpty()) {
                    try {
                        asset.setPurchaseDate(LocalDate.parse(purchaseDateStr));
                    } catch (Exception e) {
                        asset.setPurchaseDate(LocalDate.now());
                    }
                }

                String purchasePriceStr = getCellValueAsString(row.getCell(13));
                if (purchasePriceStr != null && !purchasePriceStr.isEmpty()) {
                    try {
                        asset.setPurchasePrice(new BigDecimal(purchasePriceStr));
                    } catch (Exception e) {
                        asset.setPurchasePrice(BigDecimal.ZERO);
                    }
                }

                String warrantyStr = getCellValueAsString(row.getCell(14));
                if (warrantyStr != null && !warrantyStr.isEmpty()) {
                    try {
                        asset.setWarrantyExpireDate(LocalDate.parse(warrantyStr));
                    } catch (Exception e) {
                        asset.setWarrantyExpireDate(null);
                    }
                }

                String statusStr = getCellValueAsString(row.getCell(15));
                asset.setAssetStatus(parseStatusToEnum(statusStr));

                String totalLifeStr = getCellValueAsString(row.getCell(16));
                if (totalLifeStr != null && !totalLifeStr.isEmpty()) {
                    try {
                        asset.setTotalLife(Double.parseDouble(totalLifeStr));
                    } catch (Exception e) {
                        asset.setTotalLife(10.0);
                    }
                }

                String ageStr = getCellValueAsString(row.getCell(17));
                if (ageStr != null && !ageStr.isEmpty()) {
                    try {
                        asset.setAge(Double.parseDouble(ageStr));
                    } catch (Exception e) {
                        asset.setAge(0.0);
                    }
                }

                String repairCountStr = getCellValueAsString(row.getCell(18));
                if (repairCountStr != null && !repairCountStr.isEmpty()) {
                    try {
                        asset.setRepairCount((int) Double.parseDouble(repairCountStr));
                    } catch (Exception e) {
                        asset.setRepairCount(0);
                    }
                }

                String depreciationStr = getCellValueAsString(row.getCell(19));
                if (depreciationStr != null && !depreciationStr.isEmpty()) {
                    try {
                        asset.setDepreciationValue(new BigDecimal(depreciationStr));
                    } catch (Exception e) {
                        asset.setDepreciationValue(BigDecimal.ZERO);
                    }
                }

                asset.setManagerName(getCellValueAsString(row.getCell(20)));
                asset.setMaintenanceContact(getCellValueAsString(row.getCell(21)));
                asset.setRemark(getCellValueAsString(row.getCell(22)));
                asset.setIsDeleted(0);
                assetList.add(asset);
            }

            if (!assetList.isEmpty()) {
                saveBatch(assetList);
            }
        } catch (Exception e) {
            log.error("Import assets failed", e);
            throw new BusinessException(500, "导入资产数据失败: " + e.getMessage());
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        return cell.getCellType() == CellType.STRING ? cell.getStringCellValue() :
               cell.getCellType() == CellType.NUMERIC ? String.valueOf((long) cell.getNumericCellValue()) : "";
    }

    private double getCellValueAsDouble(Cell cell) {
        if (cell == null) return 0.0;
        return cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : 0.0;
    }

    @Override
    public String checkInventory(String assetCode) {
        LambdaQueryWrapper<AssetEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AssetEntity::getAssetCode, assetCode)
                    .eq(AssetEntity::getIsDeleted, 0);

        AssetEntity asset = getOne(queryWrapper);
        if (asset != null) {
            asset.setLastInventoryTime(LocalDateTime.now());
            updateById(asset);
            return "盘点成功：资产[" + asset.getAssetName() + "]在库，状态正常。";
        } else {
            return "盘点异常：未找到资产编码[" + assetCode + "]或资产已被删除。";
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Idempotent
    public List<String> batchInventory(AssetBatchInventoryDTO dto) {
        List<String> results = new ArrayList<>();
        if (dto.getAssetCodes() == null || dto.getAssetCodes().isEmpty()) {
            return results;
        }
        for (String code : dto.getAssetCodes()) {
            results.add(this.checkInventory(code));
            try {
                this.scanAndResolve(code, dto.getOperatorId());
            } catch (BusinessException e) {
            }
        }
        return results;
    }

    @Override
    public AssetScanResultDTO scanAndResolve(String assetCode, Long operatorId) {
        LambdaQueryWrapper<AssetEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AssetEntity::getAssetCode, assetCode)
                    .or().eq(AssetEntity::getSnCode, assetCode)
                    .eq(AssetEntity::getIsDeleted, 0);
        AssetEntity asset = getOne(queryWrapper);
        if (asset == null) {
            throw new BusinessException(404, "未找到资产编码[" + assetCode + "]或资产已被删除");
        }

        asset.setLastInventoryTime(LocalDateTime.now());
        updateById(asset);

        AssetLifecycleLogEntity lifecycleLog = new AssetLifecycleLogEntity();
        lifecycleLog.setAssetId(asset.getId());
        lifecycleLog.setOperateType("SCAN");
        lifecycleLog.setOperatorId(operatorId);
        lifecycleLog.setOperateTime(LocalDateTime.now());
        lifecycleLog.setDetail("扫码查看资产信息");
        assetLifecycleLogService.save(lifecycleLog);

        LambdaQueryWrapper<AssetLifecycleLogEntity> logQuery = new LambdaQueryWrapper<>();
        logQuery.eq(AssetLifecycleLogEntity::getAssetId, asset.getId())
                .orderByDesc(AssetLifecycleLogEntity::getOperateTime)
                .last("LIMIT 10");
        List<AssetLifecycleLogEntity> recentHistory = assetLifecycleLogService.list(logQuery);

        AssetScanResultDTO dto = new AssetScanResultDTO();
        dto.setAsset(asset);
        dto.setRecentHistory(recentHistory);
        return dto;
    }

    @Transactional(rollbackFor = Exception.class)
    public void receiveAsset(Long assetId, Long operatorId, String operatorName, String targetDepartment, String targetUser) {
        AssetEntity asset = getById(assetId);
        if (asset == null) {
            throw new BusinessException(404, "资产不存在");
        }

        String oldStatus = asset.getAssetStatus();
        String detail = String.format("领用资产：从[%s]领用到[%s]，使用人：[%s]",
            oldStatus != null ? oldStatus : "入库",
            targetDepartment,
            targetUser);

        asset.setDepartment(targetDepartment);
        asset.setUserName(targetUser);
        asset.setAssetStatus(STATUS_IN_USE);
        updateById(asset);

        recordLog(assetId, "RECEIVE", operatorId, operatorName, detail);
    }

    @Transactional(rollbackFor = Exception.class)
    public void transferAsset(Long assetId, Long operatorId, String operatorName, String targetDepartment, String targetUser, String reason) {
        AssetEntity asset = getById(assetId);
        if (asset == null) {
            throw new BusinessException(404, "资产不存在");
        }

        String detail = String.format("调拨资产：从[%s]-[%s]调拨到[%s]-[%s]，原因：%s",
            asset.getDepartment(),
            asset.getUserName(),
            targetDepartment,
            targetUser,
            reason != null ? reason : "");

        asset.setDepartment(targetDepartment);
        asset.setUserName(targetUser);
        updateById(asset);

        recordLog(assetId, "TRANSFER", operatorId, operatorName, detail);
    }

    @Transactional(rollbackFor = Exception.class)
    public void returnAsset(Long assetId, Long operatorId, String operatorName, String reason) {
        AssetEntity asset = getById(assetId);
        if (asset == null) {
            throw new BusinessException(404, "资产不存在");
        }

        String detail = String.format("归还资产：使用人[%s]归还资产，原因：%s",
            asset.getUserName(),
            reason != null ? reason : "");

        asset.setUserName(null);
        asset.setAssetStatus(STATUS_IDLE);
        updateById(asset);

        recordLog(assetId, "RETURN", operatorId, operatorName, detail);
    }

    @Transactional(rollbackFor = Exception.class)
    public void repairAsset(Long assetId, Long operatorId, String operatorName, String reason) {
        AssetEntity asset = getById(assetId);
        if (asset == null) {
            throw new BusinessException(404, "资产不存在");
        }

        String detail = String.format("报修资产：[%s]，原因：%s",
            asset.getAssetName(),
            reason != null ? reason : "");

        asset.setAssetStatus(STATUS_REPAIR);
        asset.setRepairCount(asset.getRepairCount() != null ? asset.getRepairCount() + 1 : 1);
        updateById(asset);

        recordLog(assetId, "REPAIR", operatorId, operatorName, detail);
    }

    @Transactional(rollbackFor = Exception.class)
    public void scrapAsset(Long assetId, Long operatorId, String operatorName, String reason) {
        AssetEntity asset = getById(assetId);
        if (asset == null) {
            throw new BusinessException(404, "资产不存在");
        }

        String detail = String.format("报废资产：[%s]，原因：%s",
            asset.getAssetName(),
            reason != null ? reason : "");

        asset.setAssetStatus(STATUS_SCRAP);
        updateById(asset);

        recordLog(assetId, "SCRAP", operatorId, operatorName, detail);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateAssetInfo(Long assetId, Long operatorId, String operatorName, Map<String, Object> changes) {
        AssetEntity asset = getById(assetId);
        if (asset == null) {
            throw new BusinessException(404, "资产不存在");
        }

        List<String> changeDetails = new ArrayList<>();
        for (Map.Entry<String, Object> entry : changes.entrySet()) {
            String field = entry.getKey();
            Object value = entry.getValue();

            switch (field) {
                case "assetName": asset.setAssetName((String) value); break;
                case "brand": asset.setBrand((String) value); break;
                case "config": asset.setConfig((String) value); break;
                case "department": asset.setDepartment((String) value); break;
                case "userName": asset.setUserName((String) value); break;
                case "assetType": asset.setAssetType((String) value); break;
                case "assetSubtype": asset.setAssetSubtype((String) value); break;
                case "ip": asset.setIp((String) value); break;
                case "location": asset.setLocation((String) value); break;
                case "managerName": asset.setManagerName((String) value); break;
                case "maintenanceContact": asset.setMaintenanceContact((String) value); break;
                case "remark": asset.setRemark((String) value); break;
                case "purchaseDate":
                    if (value != null) {
                        try {
                            asset.setPurchaseDate(LocalDate.parse((String) value));
                        } catch (Exception e) {}
                    }
                    break;
                case "purchasePrice":
                    if (value != null) {
                        try {
                            asset.setPurchasePrice(new BigDecimal(value.toString()));
                        } catch (Exception e) {}
                    }
                    break;
                case "warrantyExpireDate":
                    if (value != null) {
                        try {
                            asset.setWarrantyExpireDate(LocalDate.parse((String) value));
                        } catch (Exception e) {}
                    }
                    break;
            }
            changeDetails.add(field + ": " + value);
        }

        updateById(asset);
        recordLog(assetId, "UPDATE", operatorId, operatorName, "修改资产信息：" + String.join(", ", changeDetails));
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createWorkOrderFromAsset(Long assetId, Long operatorId, String description, Integer urgencyLevel) {
        AssetEntity asset = getById(assetId);
        if (asset == null) {
            throw new BusinessException(404, "资产不存在");
        }

        com.company.itoms.dto.request.WorkOrderCreateDTO dto = new com.company.itoms.dto.request.WorkOrderCreateDTO();
        dto.setDescription(String.format("【%s报修】%s", asset.getAssetName(), description));
        dto.setCreatorId(operatorId);
        dto.setDepartmentId(1L);
        dto.setFaultType(1); // 1 = HARDWARE
        dto.setUrgencyLevel(urgencyLevel != null ? urgencyLevel : 2);
        dto.setWorkOrderCode("WO" + System.currentTimeMillis());

        Long workOrderId = workOrderService.createWorkOrder(dto);

        repairAsset(assetId, operatorId, "system", description);

        return workOrderId;
    }

    public Map<String, Object> getAssetStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long totalCount = count(new LambdaQueryWrapper<AssetEntity>()
            .eq(AssetEntity::getIsDeleted, 0));
        long inUseCount = count(new LambdaQueryWrapper<AssetEntity>()
            .eq(AssetEntity::getAssetStatus, STATUS_IN_USE)
            .eq(AssetEntity::getIsDeleted, 0));
        long idleCount = count(new LambdaQueryWrapper<AssetEntity>()
            .eq(AssetEntity::getAssetStatus, STATUS_IDLE)
            .eq(AssetEntity::getIsDeleted, 0));
        long repairCount = count(new LambdaQueryWrapper<AssetEntity>()
            .eq(AssetEntity::getAssetStatus, STATUS_REPAIR)
            .eq(AssetEntity::getIsDeleted, 0));
        long scrapCount = count(new LambdaQueryWrapper<AssetEntity>()
            .eq(AssetEntity::getAssetStatus, STATUS_SCRAP)
            .eq(AssetEntity::getIsDeleted, 0));

        LocalDate thirtyDaysLater = LocalDate.now().plusDays(30);
        long warrantyExpiringCount = count(new LambdaQueryWrapper<AssetEntity>()
            .le(AssetEntity::getWarrantyExpireDate, thirtyDaysLater)
            .ge(AssetEntity::getWarrantyExpireDate, LocalDate.now())
            .isNotNull(AssetEntity::getWarrantyExpireDate)
            .eq(AssetEntity::getIsDeleted, 0));

        stats.put("total", totalCount);
        stats.put("inUse", inUseCount);
        stats.put("idle", idleCount);
        stats.put("repair", repairCount);
        stats.put("scrap", scrapCount);
        stats.put("warrantyExpiring", warrantyExpiringCount);

        return stats;
    }

    public List<AssetEntity> getAssetsByStatus(String assetStatus) {
        return list(new LambdaQueryWrapper<AssetEntity>()
            .eq(AssetEntity::getAssetStatus, assetStatus)
            .eq(AssetEntity::getIsDeleted, 0));
    }

    public List<AssetEntity> getAssetsByDepartment(String department) {
        return list(new LambdaQueryWrapper<AssetEntity>()
            .eq(AssetEntity::getDepartment, department)
            .eq(AssetEntity::getIsDeleted, 0));
    }

    public List<AssetEntity> getWarrantyExpiringAssets(int days) {
        LocalDate futureDate = LocalDate.now().plusDays(days);
        return list(new LambdaQueryWrapper<AssetEntity>()
            .le(AssetEntity::getWarrantyExpireDate, futureDate)
            .ge(AssetEntity::getWarrantyExpireDate, LocalDate.now())
            .isNotNull(AssetEntity::getWarrantyExpireDate)
            .eq(AssetEntity::getIsDeleted, 0));
    }

    public List<AssetEntity> getLongTimeIdleAssets(int months) {
        return list(new LambdaQueryWrapper<AssetEntity>()
            .eq(AssetEntity::getAssetStatus, STATUS_IDLE)
            .eq(AssetEntity::getIsDeleted, 0));
    }

    private void recordLog(Long assetId, String operateType, Long operatorId, String operatorName, String detail) {
        AssetLifecycleLogEntity log = new AssetLifecycleLogEntity();
        log.setAssetId(assetId);
        log.setOperateType(operateType);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setOperateTime(LocalDateTime.now());
        log.setDetail(detail);
        assetLifecycleLogService.save(log);
    }

    private String parseAssetStatus(String status) {
        if (status == null) return "在用";
        switch (status) {
            case STATUS_IN_USE: return "在用";
            case STATUS_IDLE: return "闲置";
            case STATUS_REPAIR: return "维修中";
            case STATUS_SCRAP: return "报废";
            default: return status;
        }
    }

    private String parseStatusToEnum(String status) {
        if (status == null) return STATUS_IN_USE;
        switch (status) {
            case "在用": return STATUS_IN_USE;
            case "闲置": return STATUS_IDLE;
            case "维修中": return STATUS_REPAIR;
            case "报废": return STATUS_SCRAP;
            default: return STATUS_IN_USE;
        }
    }
}
