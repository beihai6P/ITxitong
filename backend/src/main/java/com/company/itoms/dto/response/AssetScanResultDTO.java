package com.company.itoms.dto.response;

import com.company.itoms.entity.AssetEntity;
import com.company.itoms.entity.AssetLifecycleLogEntity;
import lombok.Data;

import java.util.List;

@Data
public class AssetScanResultDTO {
    private AssetEntity asset;
    private List<AssetLifecycleLogEntity> recentHistory;

    public AssetEntity getAsset() {
        return this.asset;
    }

    public void setAsset(AssetEntity asset) {
        this.asset = asset;
    }

    public List<AssetLifecycleLogEntity> getRecentHistory() {
        return this.recentHistory;
    }

    public void setRecentHistory(List<AssetLifecycleLogEntity> recentHistory) {
        this.recentHistory = recentHistory;
    }

}
