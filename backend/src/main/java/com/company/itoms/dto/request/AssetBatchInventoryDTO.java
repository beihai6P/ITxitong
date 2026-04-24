package com.company.itoms.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class AssetBatchInventoryDTO {
    @NotEmpty(message = "Asset codes cannot be empty")
    private List<String> assetCodes;
    
    private Long operatorId;
}
