package com.company.itoms.service.impl;

import com.alibaba.fastjson2.JSON;
import com.company.itoms.dto.request.WorkOrderCreateDTO;
import com.company.itoms.dto.response.AiRecommendationDTO;
import com.company.itoms.dto.response.AiPredictionDTO;
import com.company.itoms.exception.AiApiException;
import com.company.itoms.service.AiService;
import com.company.itoms.util.AiApiUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.company.itoms.dto.request.AiFeedbackDTO;
import com.company.itoms.dto.response.AiStatsDTO;
import com.company.itoms.entity.AiRecommendationLogEntity;
import com.company.itoms.mapper.AiRecommendationLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

@Slf4j
@Service("baiduAiService")
public class BaiduAiServiceImpl implements AiService {

    @Autowired
    private AiRecommendationLogMapper aiRecommendationLogMapper;

    @Value("${ai.baidu.url:https://api.baidu.com/v1/ai/recommend}")
    private String apiUrl;

    @Override
    public AiPredictionDTO predictFault(String deviceData) {
        // Implement prediction logic
        AiPredictionDTO dto = new AiPredictionDTO();
        dto.setPredictionResult("Predicted fault for device (Baidu): " + deviceData);
        dto.setSource("Baidu_AI");
        return dto;
    }

    @Override
    public AiPredictionDTO predictWorkload(String historyData) {
        // Implement workload prediction logic
        AiPredictionDTO dto = new AiPredictionDTO();
        dto.setPredictionResult("Predicted workload based on (Baidu): " + historyData);
        dto.setSource("Baidu_AI");
        return dto;
    }

    private final OkHttpClient httpClient;

    public BaiduAiServiceImpl() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public AiRecommendationDTO recommend(WorkOrderCreateDTO dto) {
        Map<String, String> headers = AiApiUtil.generateSignHeader();
        
        String requestBodyJson = JSON.toJSONString(dto);
        RequestBody body = RequestBody.create(requestBodyJson, MediaType.parse("application/json"));
        
        Request.Builder requestBuilder = new Request.Builder()
                .url(apiUrl)
                .post(body);
                
        headers.forEach(requestBuilder::addHeader);
        
        Request request = requestBuilder.build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new AiApiException("AI API returned error code: " + response.code());
            }
            
            String responseBody = response.body() != null ? response.body().string() : "";
            
            // Assuming response is directly convertible to AiRecommendationDTO for demo
            // In reality, you'd parse a complex response structure
            AiRecommendationDTO recommendation = JSON.parseObject(responseBody, AiRecommendationDTO.class);
            if (recommendation == null) {
                throw new AiApiException("Failed to parse AI response");
            }
            recommendation.setSource("AI_GENERATED");
            return recommendation;
        } catch (IOException e) {
            throw new AiApiException("Failed to call AI API", e);
        }
    }

    @Override
    public void aiFeedback(AiFeedbackDTO dto) {
        AiRecommendationLogEntity logEntity = aiRecommendationLogMapper.selectById(dto.getLogId());
        if (logEntity != null) {
            logEntity.setUserFeedback(dto.getUserFeedback());
            aiRecommendationLogMapper.updateById(logEntity);
        }
    }

    @Override
    public AiStatsDTO aiStats() {
        Long total = aiRecommendationLogMapper.selectCount(new LambdaQueryWrapper<>());
        Long accepted = aiRecommendationLogMapper.selectCount(new LambdaQueryWrapper<AiRecommendationLogEntity>()
                .eq(AiRecommendationLogEntity::getUserFeedback, 1));
                
        AiStatsDTO stats = new AiStatsDTO();
        stats.setTotalRecommendations(total != null ? total : 0L);
        stats.setAcceptedRecommendations(accepted != null ? accepted : 0L);
        if (total != null && total > 0) {
            stats.setAcceptanceRate((double) (accepted != null ? accepted : 0L) / total);
        } else {
            stats.setAcceptanceRate(0.0);
        }
        return stats;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BaiduAiServiceImpl.class);

}
