package com.company.itoms.dto.response;

import lombok.Data;

@Data
public class AiStatsDTO {
    private Long totalRecommendations;
    private Long acceptedRecommendations;
    private Double acceptanceRate;
}
