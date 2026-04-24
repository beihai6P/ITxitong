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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.company.itoms.dto.request.AssetBatchInventoryDTO;
import com.company.itoms.common.Idempotent;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssetServiceImpl extends ServiceImpl<AssetMapper, AssetEntity> implements AssetService {

    @Autowired
    private AssetLifecycleLogService assetLifecycleLogService;

    @Override
    public Double calculateHealth(Long assetId) {
        AssetEntity asset = getById(assetId);
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
            // Include asset code and name in QR Code
            String qrContent = String.format("AssetCode:%s,Name:%s", asset.getAssetCode(), asset.getAssetName());
            
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
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("ID");
            headerRow.createCell(1).setCellValue("资产编码");
            headerRow.createCell(2).setCellValue("资产名称");
            headerRow.createCell(3).setCellValue("已用年限");
            headerRow.createCell(4).setCellValue("总寿命");
            headerRow.createCell(5).setCellValue("维修次数");
            headerRow.createCell(6).setCellValue("状态");
            
            // Create data rows
            int rowNum = 1;
            for (AssetEntity asset : assets) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(asset.getId() != null ? asset.getId() : 0);
                row.createCell(1).setCellValue(asset.getAssetCode() != null ? asset.getAssetCode() : "");
                row.createCell(2).setCellValue(asset.getAssetName() != null ? asset.getAssetName() : "");
                row.createCell(3).setCellValue(asset.getAge() != null ? asset.getAge() : 0.0);
                row.createCell(4).setCellValue(asset.getTotalLife() != null ? asset.getTotalLife() : 0.0);
                row.createCell(5).setCellValue(asset.getRepairCount() != null ? asset.getRepairCount() : 0);
                row.createCell(6).setCellValue(asset.getIsDeleted() != null && asset.getIsDeleted() == 1 ? "已删" : "正常");
            }
            
            // Setup response
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
            
            // Start from row 1 (skip header)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                AssetEntity asset = new AssetEntity();
                
                Cell codeCell = row.getCell(1);
                if (codeCell != null) asset.setAssetCode(getCellValueAsString(codeCell));
                
                Cell nameCell = row.getCell(2);
                if (nameCell != null) asset.setAssetName(getCellValueAsString(nameCell));
                
                Cell ageCell = row.getCell(3);
                if (ageCell != null) asset.setAge(getCellValueAsDouble(ageCell));
                
                Cell totalLifeCell = row.getCell(4);
                if (totalLifeCell != null) asset.setTotalLife(getCellValueAsDouble(totalLifeCell));
                
                Cell repairCountCell = row.getCell(5);
                if (repairCountCell != null) asset.setRepairCount((int) getCellValueAsDouble(repairCountCell));
                
                asset.setIsDeleted(0);
                assetList.add(asset);
            }
            
            if (!assetList.isEmpty()) {
                saveBatch(assetList);
            }
        } catch (Exception e) {
            throw new BusinessException(500, "导入资产数据失败");
        }
    }
    
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        return cell.getCellType() == CellType.STRING ? cell.getStringCellValue() : String.valueOf(cell.getNumericCellValue());
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
            
            // Optionally record scan operation for each code
            try {
                this.scanAndResolve(code, dto.getOperatorId());
            } catch (BusinessException e) {
                // If not found, skip logging
            }
        }
        return results;
    }

    @Override
    public AssetScanResultDTO scanAndResolve(String assetCode, Long operatorId) {
        LambdaQueryWrapper<AssetEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AssetEntity::getAssetCode, assetCode)
                    .eq(AssetEntity::getIsDeleted, 0);
        AssetEntity asset = getOne(queryWrapper);
        if (asset == null) {
            throw new BusinessException(404, "未找到资产编码[" + assetCode + "]或资产已被删除");
        }

        // Record the scan operation
        AssetLifecycleLogEntity log = new AssetLifecycleLogEntity();
        log.setAssetId(asset.getId());
        log.setAction("SCAN");
        log.setOperatorId(operatorId);
        log.setRemark("扫码查看资产信息");
        assetLifecycleLogService.save(log);

        // Fetch recent history (top 10)
        LambdaQueryWrapper<AssetLifecycleLogEntity> logQuery = new LambdaQueryWrapper<>();
        logQuery.eq(AssetLifecycleLogEntity::getAssetId, asset.getId())
                .orderByDesc(AssetLifecycleLogEntity::getCreateTime)
                .last("LIMIT 10");
        List<AssetLifecycleLogEntity> recentHistory = assetLifecycleLogService.list(logQuery);

        AssetScanResultDTO dto = new AssetScanResultDTO();
        dto.setAsset(asset);
        dto.setRecentHistory(recentHistory);
        return dto;
    }
}
