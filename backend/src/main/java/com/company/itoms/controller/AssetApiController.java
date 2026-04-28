package com.company.itoms.controller;

import com.company.itoms.common.Result;
import com.company.itoms.dto.response.AssetScanResultDTO;
import com.company.itoms.entity.AssetEntity;
import com.company.itoms.entity.AssetLifecycleLogEntity;
import com.company.itoms.service.AssetService;
import com.company.itoms.service.AssetLifecycleLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

@RestController
@RequestMapping("/api/asset")
public class AssetApiController {
    @Autowired
    private AssetService assetService;

    @Autowired
    private AssetLifecycleLogService assetLifecycleLogService;

    @GetMapping("/scan/resolve")
    public Result<AssetScanResultDTO> scanAndResolve(@RequestParam("assetCode") String assetCode,
                                                     @RequestParam(value = "operatorId", defaultValue = "1") Long operatorId) {
        return Result.success(assetService.scanAndResolve(assetCode, operatorId));
    }

    @GetMapping("/{id}")
    public Result<AssetEntity> getAsset(@PathVariable Long id) {
        return Result.success(assetService.getById(id));
    }

    @GetMapping("/{id}/health")
    public Result<Double> getAssetHealth(@PathVariable Long id) {
        return Result.success(assetService.calculateHealth(id));
    }

    @GetMapping("/{id}/qrcode")
    public ResponseEntity<byte[]> getAssetQRCode(@PathVariable Long id) {
        byte[] qrCode = assetService.generateAssetQRCode(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"qrcode_" + id + ".png\"")
                .contentType(MediaType.IMAGE_PNG)
                .body(qrCode);
    }

    @GetMapping("/export")
    public void exportAssets(HttpServletResponse response) {
        assetService.exportAssets(response);
    }

    @PostMapping("/import")
    public Result<String> importAssets(@RequestParam("file") MultipartFile file) {
        assetService.importAssets(file);
        return Result.success("导入成功");
    }

    @GetMapping("/inventory-check")
    public Result<String> checkInventory(@RequestParam("assetCode") String assetCode) {
        return Result.success(assetService.checkInventory(assetCode));
    }

    @GetMapping("/{id}/logs")
    public Result<List<AssetLifecycleLogEntity>> getAssetLogs(@PathVariable Long id) {
        return Result.success(assetLifecycleLogService.getLogsByAssetId(id));
    }
}