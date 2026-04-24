package com.company.itoms.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.itoms.common.SPI;
import com.company.itoms.dto.request.WorkOrderCreateDTO;
import com.company.itoms.dto.response.AiRecommendationDTO;
import com.company.itoms.dto.response.AiPredictionDTO;
import com.company.itoms.entity.AiApiConfigEntity;
import com.company.itoms.exception.AiApiException;
import com.company.itoms.mapper.AiApiConfigMapper;
import com.company.itoms.service.AiService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.company.itoms.dto.request.AiFeedbackDTO;
import com.company.itoms.dto.response.AiStatsDTO;
import com.company.itoms.entity.AiRecommendationLogEntity;
import com.company.itoms.mapper.AiRecommendationLogMapper;

@Slf4j
@SPI("aliyun_ai")
@Service("aliyunAiService")
public class AliyunAiServiceImpl implements AiService {

    @Autowired
    private AiRecommendationLogMapper aiRecommendationLogMapper;

    private final OkHttpClient httpClient;
    
    @Autowired
    private AiApiConfigMapper aiApiConfigMapper;

    public AliyunAiServiceImpl() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    private String callAliyunApi(String prompt) {
        LambdaQueryWrapper<AiApiConfigEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AiApiConfigEntity::getProviderName, "aliyun")
                .eq(AiApiConfigEntity::getIsActive, 1)
                .last("LIMIT 1");
        
        AiApiConfigEntity config = aiApiConfigMapper.selectOne(queryWrapper);
        if (config == null) {
            throw new AiApiException("Aliyun AI configuration not found or inactive.");
        }

        String apiUrl = config.getApiUrl();
        String apiKey = config.getApiKey();

        JSONObject requestBodyJson = new JSONObject();
        requestBodyJson.put("model", config.getModelName() != null ? config.getModelName() : "qwen-turbo");
        
        JSONObject input = new JSONObject();
        input.put("prompt", prompt);
        requestBodyJson.put("input", input);

        RequestBody body = RequestBody.create(requestBodyJson.toJSONString(), MediaType.parse("application/json"));
        
        Request request = new Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();
                
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new AiApiException("Aliyun AI API returned error code: " + response.code());
            }
            return response.body() != null ? response.body().string() : "";
        } catch (IOException e) {
            throw new AiApiException("Failed to call Aliyun AI API", e);
        }
    }

    private String parseAiResponseText(String responseBody) {
        JSONObject jsonResponse = JSON.parseObject(responseBody);
        if (jsonResponse != null && jsonResponse.containsKey("output")) {
            JSONObject output = jsonResponse.getJSONObject("output");
            if (output != null && output.containsKey("text")) {
                return output.getString("text");
            }
        }
        return "No solution found.";
    }

    @Override
    public AiPredictionDTO predictFault(String deviceData) {
        String prompt = "请根据以下设备数据和历史记录，预测可能发生的故障及健康状况：" + deviceData;
        String responseBody = callAliyunApi(prompt);
        String content = parseAiResponseText(responseBody);
        
        AiPredictionDTO prediction = new AiPredictionDTO();
        prediction.setPredictionResult(content);
        prediction.setConfidence(0.90);
        prediction.setSource("ALIYUN_AI");
        prediction.setRawAiResponse(responseBody);
        return prediction;
    }

    @Override
    public AiPredictionDTO predictWorkload(String historyData) {
        String prompt = "请根据以下历史工单和运维数据，预测未来的工作负载趋势：" + historyData;
        String responseBody = callAliyunApi(prompt);
        String content = parseAiResponseText(responseBody);
        
        AiPredictionDTO prediction = new AiPredictionDTO();
        prediction.setPredictionResult(content);
        prediction.setConfidence(0.85);
        prediction.setSource("ALIYUN_AI");
        prediction.setRawAiResponse(responseBody);
        return prediction;
    }

    @Override
    public AiRecommendationDTO recommend(WorkOrderCreateDTO dto) {
        String prompt = "请根据以下故障信息提供解决方案推荐：" + dto.getDescription();
        String responseBody = callAliyunApi(prompt);
        String content = parseAiResponseText(responseBody);
        
        AiRecommendationDTO recommendation = new AiRecommendationDTO();
        recommendation.setContent(content);
        recommendation.setConfidence(0.85);
        recommendation.setSource("AI_GENERATED");
        recommendation.setRawAiResponse(responseBody);
        
        return recommendation;
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
}
