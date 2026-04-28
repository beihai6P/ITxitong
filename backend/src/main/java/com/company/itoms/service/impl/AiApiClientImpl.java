package com.company.itoms.service.impl;

import com.company.itoms.config.AiConfigProperties;
import com.company.itoms.dto.AiRecommendationDTO;
import com.company.itoms.dto.request.WorkOrderCreateDTO;
import com.company.itoms.entity.KnowledgeBaseEntity;
import com.company.itoms.exception.AiApiException;
import com.company.itoms.mapper.KnowledgeBaseMapper;
import com.company.itoms.service.AiApiClient;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.company.itoms.mapper.AssetMapper;
import com.company.itoms.entity.AssetEntity;
import com.company.itoms.entity.SysDictEntity;
import com.company.itoms.mapper.SysDictMapper;
import java.util.List;

@Slf4j
@Component
public class AiApiClientImpl implements AiApiClient {

    @Autowired
    private AiConfigProperties aiConfig;

    @Autowired
    private KnowledgeBaseMapper knowledgeBaseMapper;

    @Autowired
    private AssetMapper assetMapper;

    @Autowired
    private SysDictMapper sysDictMapper;

    @Override
    public AiRecommendationDTO call(WorkOrderCreateDTO request) throws AiApiException {
        if (!aiConfig.isEnabled()) {
            log.info("AI service is disabled, using knowledge base fallback");
            return fallbackToKnowledgeBase(request);
        }

        for (int attempt = 1; attempt <= aiConfig.getRetryCount(); attempt++) {
            try {
                return callAiApi(request);
            } catch (Exception e) {
                log.warn("AI API call failed (attempt {}/{}): {}", attempt, aiConfig.getRetryCount(), e.getMessage());
                if (attempt == aiConfig.getRetryCount()) {
                    log.error("All AI API retries exhausted, falling back to knowledge base");
                    return fallbackToKnowledgeBase(request);
                }
                try {
                    Thread.sleep(aiConfig.getRetryDelay() * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        return fallbackToKnowledgeBase(request);
    }

    private AiRecommendationDTO callAiApi(WorkOrderCreateDTO request) throws AiApiException {
        throw new AiApiException("AI API service not implemented, using fallback");
    }

    private AiRecommendationDTO fallbackToKnowledgeBase(WorkOrderCreateDTO request) {
        String description = request.getDescription();
        if (description == null || description.isEmpty()) {
            return createDefaultFallback();
        }

        Long categoryId = null;
        if (request.getAssetId() != null) {
            AssetEntity asset = assetMapper.selectById(request.getAssetId());
            if (asset != null && asset.getAssetType() != null) {
                LambdaQueryWrapper<SysDictEntity> dictQuery = new LambdaQueryWrapper<>();
                dictQuery.eq(SysDictEntity::getDictCode, "ASSET_CATEGORY")
                         .eq(SysDictEntity::getDictValue, asset.getAssetType());
                SysDictEntity dict = sysDictMapper.selectOne(dictQuery);
                if (dict != null) {
                    categoryId = dict.getId();
                }
            }
        }

        LambdaQueryWrapper<KnowledgeBaseEntity> kbQuery = new LambdaQueryWrapper<>();
        kbQuery.eq(KnowledgeBaseEntity::getReviewStatus, 1);
        if (categoryId != null) {
            kbQuery.eq(KnowledgeBaseEntity::getAssetCategoryId, categoryId);
        }
        
        List<KnowledgeBaseEntity> knowledgeList = knowledgeBaseMapper.selectList(kbQuery);
        
        // 降级：如果带分类查不到，查全局
        if (knowledgeList.isEmpty() && categoryId != null) {
            LambdaQueryWrapper<KnowledgeBaseEntity> fallbackQuery = new LambdaQueryWrapper<>();
            fallbackQuery.eq(KnowledgeBaseEntity::getReviewStatus, 1);
            knowledgeList = knowledgeBaseMapper.selectList(fallbackQuery);
        }

        if (knowledgeList.isEmpty()) {
            return createDefaultFallback();
        }

        KnowledgeBaseEntity bestMatch = null;
        double highestScore = 0;

        for (KnowledgeBaseEntity kb : knowledgeList) {
            double score = calculateMatchScore(description, kb, request);
            if (score > highestScore) {
                highestScore = score;
                bestMatch = kb;
            }
        }

        if (bestMatch != null && highestScore > 0.3) {
            AiRecommendationDTO recommendation = new AiRecommendationDTO();
            recommendation.setSolutionId(bestMatch.getId());
            recommendation.setConfidence(Math.min(highestScore, 0.9));
            recommendation.setSource("KNOWLEDGE_BASE");
            recommendation.setInternalDebugInfo(bestMatch.getContent());
            log.info("Found knowledge base match with score {} for description: {}", highestScore, description.substring(0, Math.min(50, description.length())));
            return recommendation;
        }

        return createDefaultFallback();
    }

    private double calculateMatchScore(String description, KnowledgeBaseEntity kb, WorkOrderCreateDTO request) {
        double score = 0;
        int matchCount = 0;

        String symptom = kb.getTitle();
        if (symptom != null && !symptom.isEmpty()) {
            String[] keywords = symptom.toLowerCase().split("[\\s,，。；;、]+");
            for (String keyword : keywords) {
                if (keyword.length() >= 2 && description.toLowerCase().contains(keyword)) {
                    matchCount++;
                }
            }
            score = (double) matchCount / Math.max(keywords.length, 1);
        }

        if (kb.getFaultType() != null && request.getFaultType() != null) {
            String faultTypeStr;
            switch (request.getFaultType()) {
                case 1: faultTypeStr = "HARDWARE"; break;
                case 2: faultTypeStr = "SOFTWARE"; break;
                case 3: faultTypeStr = "NETWORK"; break;
                case 4: faultTypeStr = "SYSTEM"; break;
                case 5: faultTypeStr = "OTHER"; break;
                default: faultTypeStr = "OTHER";
            }
            if (kb.getFaultType().equals(faultTypeStr)) {
                score *= 1.2;
            }
        }

        // 使用权重作为乘数因子，默认100为基准
        if (kb.getWeight() != null && kb.getWeight() > 0) {
            score *= (kb.getWeight() / 100.0);
        }

        return Math.min(score, 1.0);
    }

    private AiRecommendationDTO createDefaultFallback() {
        AiRecommendationDTO recommendation = new AiRecommendationDTO();
        recommendation.setSolutionId(999L);
        recommendation.setConfidence(0.5);
        recommendation.setSource("FALLBACK");
        recommendation.setInternalDebugInfo("请联系技术支持获取帮助");
        return recommendation;
    }
}