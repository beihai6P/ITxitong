package com.company.itoms.service;

import com.company.itoms.dto.AiRecommendationDTO;
import com.company.itoms.dto.request.WorkOrderCreateDTO;
import com.company.itoms.exception.AiApiException;

public interface AiApiClient {
    AiRecommendationDTO call(WorkOrderCreateDTO dto) throws AiApiException;
}
