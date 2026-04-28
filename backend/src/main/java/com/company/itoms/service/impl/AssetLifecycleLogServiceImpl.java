package com.company.itoms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.itoms.entity.AssetLifecycleLogEntity;
import com.company.itoms.mapper.AssetLifecycleLogMapper;
import com.company.itoms.service.AssetLifecycleLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AssetLifecycleLogServiceImpl extends ServiceImpl<AssetLifecycleLogMapper, AssetLifecycleLogEntity> implements AssetLifecycleLogService {

    @Override
    public void recordLog(Long assetId, String operateType, Long operatorId, String operatorName, String detail) {
        AssetLifecycleLogEntity lifecycleLog = new AssetLifecycleLogEntity();
        lifecycleLog.setAssetId(assetId);
        lifecycleLog.setOperateType(operateType);
        lifecycleLog.setOperatorId(operatorId);
        lifecycleLog.setOperatorName(operatorName);
        lifecycleLog.setOperateTime(LocalDateTime.now());
        lifecycleLog.setDetail(detail);
        this.save(lifecycleLog);
    }

    @Override
    public List<AssetLifecycleLogEntity> getLogsByAssetId(Long assetId) {
        LambdaQueryWrapper<AssetLifecycleLogEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AssetLifecycleLogEntity::getAssetId, assetId)
                    .orderByDesc(AssetLifecycleLogEntity::getOperateTime);
        return this.list(queryWrapper);
    }

    @Override
    public List<AssetLifecycleLogEntity> getLogsByOperateType(String operateType) {
        LambdaQueryWrapper<AssetLifecycleLogEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AssetLifecycleLogEntity::getOperateType, operateType)
                    .orderByDesc(AssetLifecycleLogEntity::getOperateTime);
        return this.list(queryWrapper);
    }
}