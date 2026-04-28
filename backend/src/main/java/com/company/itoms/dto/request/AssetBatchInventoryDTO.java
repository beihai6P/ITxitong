package com.company.itoms.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class AssetBatchInventoryDTO {
    @NotEmpty(message = "Asset codes cannot be empty")
    private List<String> assetCodes;
    
    private Long operatorId;

    public List<String> getAssetCodes() {
        return this.assetCodes;
    }

    public void setAssetCodes(List<String> assetCodes) {
        this.assetCodes = assetCodes;
    }

    public Long getOperatorId() {
        return this.operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

}
