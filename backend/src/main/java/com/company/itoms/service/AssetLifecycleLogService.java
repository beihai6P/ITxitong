package com.company.itoms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.company.itoms.entity.AssetLifecycleLogEntity;
import java.util.List;

public interface AssetLifecycleLogService extends IService<AssetLifecycleLogEntity> {

    void recordLog(Long assetId, String operateType, Long operatorId, String operatorName, String detail);

    List<AssetLifecycleLogEntity> getLogsByAssetId(Long assetId);

    List<AssetLifecycleLogEntity> getLogsByOperateType(String operateType);
}