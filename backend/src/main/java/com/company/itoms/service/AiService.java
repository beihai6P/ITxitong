package com.company.itoms.service;

import com.company.itoms.common.SPI;
import com.company.itoms.dto.request.WorkOrderCreateDTO;
import com.company.itoms.dto.response.AiRecommendationDTO;
import com.company.itoms.dto.response.AiPredictionDTO;
import com.company.itoms.dto.request.AiFeedbackDTO;
import com.company.itoms.dto.response.AiStatsDTO;

@SPI("baidu_ai")
public interface AiService {
    AiRecommendationDTO recommend(WorkOrderCreateDTO dto);
    
    AiPredictionDTO predictFault(String deviceData);
    
    AiPredictionDTO predictWorkload(String historyData);
    
    void aiFeedback(AiFeedbackDTO dto);
    
    AiStatsDTO aiStats();
}
