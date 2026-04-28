package com.company.itoms.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.itoms.entity.AssetEntity;
import com.company.itoms.entity.AssetLifecycleLogEntity;
import com.company.itoms.event.DepartmentDeleteEvent;
import com.company.itoms.event.UserDeleteEvent;
import com.company.itoms.event.UserStatusChangeEvent;
import com.company.itoms.service.AssetService;
import com.company.itoms.mapper.AssetLifecycleLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AssetLifecycleEventListener {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(AssetLifecycleEventListener.class);
    private final AssetService assetService;
    private final AssetLifecycleLogMapper logMapper;

    @Async
    @EventListener
    public void handleUserStatusChange(UserStatusChangeEvent event) {
        try {
            boolean statusDisabled = event.getNewUser().getStatus() != null && event.getNewUser().getStatus() == 0;
            boolean isDeleted = event.getNewUser().getIsDeleted() != null && event.getNewUser().getIsDeleted() == 1;

            if (statusDisabled || isDeleted) {
                unbindAssetsByUserId(event.getNewUser().getId(), "用户离职或禁用，系统自动回收资产");
            }
        } catch (Exception e) {
            log.error("用户状态变更联动资产解绑异常，降级处理：", e);
        }
    }

    @Async
    @EventListener
    public void handleUserDelete(UserDeleteEvent event) {
        try {
            unbindAssetsByUserId(event.getUserId(), "用户删除，系统自动回收资产");
        } catch (Exception e) {
            log.error("用户删除联动资产解绑异常，降级处理：", e);
        }
    }

    @Async
    @EventListener
    public void handleDepartmentDelete(DepartmentDeleteEvent event) {
        try {
            LambdaQueryWrapper<AssetEntity> query = new LambdaQueryWrapper<>();
        query.eq(AssetEntity::getDepartmentId, event.getDeptId());
        query.ne(AssetEntity::getStatus, "2"); // 幂等：跳过已闲置资产
        List<AssetEntity> assets = assetService.list(query);

        int batchSize = 100;
        for (int i = 0; i < assets.size(); i += batchSize) {
            int end = Math.min(assets.size(), i + batchSize);
            List<AssetEntity> batch = assets.subList(i, end);
            for (AssetEntity asset : batch) {
                try {
                    asset.setDepartmentId(null);
                    asset.setStatus("2"); // 闲置状态
                    assetService.updateById(asset);
                    recordAssetLog(asset.getId(), "SYSTEM_RECYCLE", "部门撤销，系统自动回收资产");
                } catch (Exception e) {
                    log.error("资产[{}]解绑异常，跳过：{}", asset.getId(), e.getMessage());
                }
            }
        }
        } catch (Exception e) {
            log.error("部门撤销联动资产解绑异常，降级处理：", e);
        }
    }

    private void unbindAssetsByUserId(Long userId, String reason) {
        LambdaQueryWrapper<AssetEntity> query = new LambdaQueryWrapper<>();
        query.eq(AssetEntity::getUserId, userId);
        
        // 增加幂等控制：仅查询未闲置状态的资产进行解绑
        query.ne(AssetEntity::getStatus, "2"); 
        
        List<AssetEntity> assets = assetService.list(query);

        // 引入分批处理，每批100条
        int batchSize = 100;
        for (int i = 0; i < assets.size(); i += batchSize) {
            int end = Math.min(assets.size(), i + batchSize);
            List<AssetEntity> batch = assets.subList(i, end);
            for (AssetEntity asset : batch) {
                try {
                    asset.setUserId(null);
                    asset.setStatus("2"); // 2-闲置
                    assetService.updateById(asset);
                    recordAssetLog(asset.getId(), "SYSTEM_RECYCLE", reason);
                } catch (Exception e) {
                    log.error("资产[{}]解绑异常，跳过并继续处理：{}", asset.getId(), e.getMessage());
                }
            }
        }
    }

    private void recordAssetLog(Long assetId, String action, String reason) {
        AssetLifecycleLogEntity logEntity = new AssetLifecycleLogEntity();
        logEntity.setAssetId(assetId);
        logEntity.setOperatorId(0L);
        logEntity.setOperatorName("系统");
        logEntity.setOperateType(action);
        logEntity.setDetail(reason);
        logEntity.setOperateTime(LocalDateTime.now());
        logMapper.insert(logEntity);
    }
}
