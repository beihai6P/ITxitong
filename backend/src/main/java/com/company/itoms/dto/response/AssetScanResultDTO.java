package com.company.itoms.dto.response;

import com.company.itoms.entity.AssetEntity;
import com.company.itoms.entity.AssetLifecycleLogEntity;
import lombok.Data;

import java.util.List;

@Data
public class AssetScanResultDTO {
    private AssetEntity asset;
    private List<AssetLifecycleLogEntity> recentHistory;
}
