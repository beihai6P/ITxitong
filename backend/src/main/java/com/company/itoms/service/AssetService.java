package com.company.itoms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.company.itoms.entity.AssetEntity;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletResponse;

import com.company.itoms.dto.request.AssetBatchInventoryDTO;
import java.util.List;

public interface AssetService extends IService<AssetEntity> {
    Double calculateHealth(Long assetId);
    
    /**
     * Generate QR code for asset
     * @param assetId asset ID
     * @return Base64 encoded image string or byte array
     */
    byte[] generateAssetQRCode(Long assetId);
    
    /**
     * Export assets to Excel
     * @param response HTTP response
     */
    void exportAssets(HttpServletResponse response);
    
    /**
     * Import assets from Excel
     * @param file uploaded Excel file
     */
    void importAssets(MultipartFile file);
    
    /**
     * Inventory check (compare expected assets vs scanned/actual assets)
     * For simplicity, let's assume we pass an asset code to check its existence and status
     * @param assetCode the code of the asset to check
     * @return check result message
     */
    String checkInventory(String assetCode);

    /**
     * Batch Inventory check
     * @param dto the batch inventory DTO
     * @return list of check result messages
     */
    List<String> batchInventory(AssetBatchInventoryDTO dto);

    /**
     * Scan and resolve asset details and recent history
     * @param assetCode the code from QR code or barcode
     * @param operatorId the user ID who scans the asset
     * @return AssetScanResultDTO containing asset details and recent history
     */
    com.company.itoms.dto.response.AssetScanResultDTO scanAndResolve(String assetCode, Long operatorId);
}
