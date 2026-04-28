package com.company.itoms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.company.itoms.entity.AssetEntity;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletResponse;

import com.company.itoms.dto.request.AssetBatchInventoryDTO;
import java.util.List;
import java.util.Map;

public interface AssetService extends IService<AssetEntity> {
    Double calculateHealth(Long assetId);

    byte[] generateAssetQRCode(Long assetId);

    void exportAssets(HttpServletResponse response);

    void importAssets(MultipartFile file);

    String checkInventory(String assetCode);

    List<String> batchInventory(AssetBatchInventoryDTO dto);

    com.company.itoms.dto.response.AssetScanResultDTO scanAndResolve(String assetCode, Long operatorId);

    void receiveAsset(Long assetId, Long operatorId, String operatorName, String targetDepartment, String targetUser);

    void transferAsset(Long assetId, Long operatorId, String operatorName, String targetDepartment, String targetUser, String reason);

    void returnAsset(Long assetId, Long operatorId, String operatorName, String reason);

    void repairAsset(Long assetId, Long operatorId, String operatorName, String reason);

    void scrapAsset(Long assetId, Long operatorId, String operatorName, String reason);

    void updateAssetInfo(Long assetId, Long operatorId, String operatorName, Map<String, Object> changes);

    Long createWorkOrderFromAsset(Long assetId, Long operatorId, String description, Integer urgencyLevel);

    Map<String, Object> getAssetStatistics();

    List<AssetEntity> getAssetsByStatus(String assetStatus);

    List<AssetEntity> getAssetsByDepartment(String department);

    List<AssetEntity> getWarrantyExpiringAssets(int days);

    List<AssetEntity> getLongTimeIdleAssets(int months);
}
