package com.company.itoms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.company.itoms.entity.AssetLifecycleLogEntity;
import java.util.List;

public interface AssetLifecycleLogService extends IService<AssetLifecycleLogEntity> {
    
    void recordLog(Long assetId, String action, Long operatorId, String remark);
    
    List<AssetLifecycleLogEntity> getLogsByAssetId(Long assetId);
}