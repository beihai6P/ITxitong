package com.company.itoms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.itoms.entity.AssetLifecycleLogEntity;
import com.company.itoms.mapper.AssetLifecycleLogMapper;
import com.company.itoms.service.AssetLifecycleLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AssetLifecycleLogServiceImpl extends ServiceImpl<AssetLifecycleLogMapper, AssetLifecycleLogEntity> implements AssetLifecycleLogService {

    @Override
    public void recordLog(Long assetId, String action, Long operatorId, String remark) {
        AssetLifecycleLogEntity log = new AssetLifecycleLogEntity();
        log.setAssetId(assetId);
        log.setAction(action);
        log.setOperatorId(operatorId);
        log.setRemark(remark);
        log.setCreateTime(LocalDateTime.now());
        this.save(log);
    }

    @Override
    public List<AssetLifecycleLogEntity> getLogsByAssetId(Long assetId) {
        LambdaQueryWrapper<AssetLifecycleLogEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AssetLifecycleLogEntity::getAssetId, assetId)
                    .orderByDesc(AssetLifecycleLogEntity::getCreateTime);
        return this.list(queryWrapper);
    }
}