package com.company.itoms.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.itoms.entity.AiRecommendationLogEntity;
import com.company.itoms.entity.AssetEntity;
import com.company.itoms.entity.KnowledgeBaseEntity;
import com.company.itoms.entity.WorkOrderEntity;
import com.company.itoms.event.WorkOrderEvaluateEvent;
import com.company.itoms.mapper.AiRecommendationLogMapper;
import com.company.itoms.mapper.AssetMapper;
import com.company.itoms.service.AssetService;
import com.company.itoms.service.KnowledgeBaseService;
import com.company.itoms.service.WorkOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WorkOrderEvaluateEventListener {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(WorkOrderEvaluateEventListener.class);
    private final AiRecommendationLogMapper aiLogMapper;
    private final KnowledgeBaseService kbService;
    private final AssetService assetService;
    private final AssetMapper assetMapper;
    private final WorkOrderService workOrderService;

    @Async
    @EventListener
    public void handleWorkOrderEvaluate(WorkOrderEvaluateEvent event) {
        try {
            WorkOrderEntity wo = event.getWorkOrder();
            Integer score = wo.getEvaluateScore();
            if (score == null) return;

            // 1. 回写 AI 推荐日志
            LambdaQueryWrapper<AiRecommendationLogEntity> logQuery = new LambdaQueryWrapper<>();
            logQuery.eq(AiRecommendationLogEntity::getWorkOrderId, wo.getId());
            List<AiRecommendationLogEntity> aiLogs = aiLogMapper.selectList(logQuery);
            
            for (AiRecommendationLogEntity aiLog : aiLogs) {
                aiLog.setUserFeedback(score);
                aiLogMapper.updateById(aiLog);

                // 2. 调整知识库词条权重
                if (aiLog.getRecommendedSolutionId() != null) {
                    KnowledgeBaseEntity kb = kbService.getById(aiLog.getRecommendedSolutionId());
                    if (kb != null) {
                        if (score <= 2) {
                            kb.setWeight(kb.getWeight() - 10);
                            if (kb.getWeight() <= 0) {
                                kb.setReviewStatus(0); // 待复核
                            }
                        } else if (score >= 4) {
                            kb.setWeight(kb.getWeight() + 5);
                        }
                        kbService.updateById(kb);
                    }
                }
            }

            // 3. 重算资产健康度
            if (wo.getAssetId() != null) {
                recalculateAssetHealth(wo.getAssetId());
            }

        } catch (Exception e) {
            log.error("工单验收异步联动异常，降级处理，不阻塞主流程：", e);
        }
    }

    private void recalculateAssetHealth(Long assetId) {
        AssetEntity asset = assetService.getById(assetId);
        if (asset == null) return;

        // 计算历史维修次数
        LambdaQueryWrapper<WorkOrderEntity> woQuery = new LambdaQueryWrapper<>();
        woQuery.eq(WorkOrderEntity::getAssetId, assetId)
               .eq(WorkOrderEntity::getStatus, 5); // COMPLETED
        long repairCount = workOrderService.count(woQuery);

        String oldHealth = asset.getHealthStatus();
        if (repairCount > 5) {
            asset.setHealthStatus("POOR");
        } else if (repairCount > 2) {
            asset.setHealthStatus("NORMAL");
        } else {
            asset.setHealthStatus("GOOD");
        }

        if (!oldHealth.equals(asset.getHealthStatus())) {
            assetService.updateById(asset);
            
            if ("POOR".equals(asset.getHealthStatus())) {
                log.warn("【系统预警】资产 [{}] 健康度跌破阈值，触发预防性维保！", asset.getAssetCode());
                WorkOrderEntity newWo = new WorkOrderEntity();
                newWo.setWorkOrderCode("PM-" + System.currentTimeMillis());
                newWo.setAssetId(assetId);
                newWo.setFaultType("预防性维保");
                newWo.setDescription("系统自动生成：资产健康度跌破阈值，请排查");
                newWo.setUrgencyLevel(1);
                newWo.setStatus(1); // PENDING
                workOrderService.save(newWo);
                workOrderService.autoAssignWorkOrder(newWo.getId());
            }
        }
    }
}
