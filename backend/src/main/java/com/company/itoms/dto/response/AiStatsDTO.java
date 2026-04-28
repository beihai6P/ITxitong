package com.company.itoms.dto.response;

import lombok.Data;

@Data
public class AiStatsDTO {
    private Long totalRecommendations;
    private Long acceptedRecommendations;
    private Double acceptanceRate;

    public Long getTotalRecommendations() {
        return this.totalRecommendations;
    }

    public void setTotalRecommendations(Long totalRecommendations) {
        this.totalRecommendations = totalRecommendations;
    }

    public Long getAcceptedRecommendations() {
        return this.acceptedRecommendations;
    }

    public void setAcceptedRecommendations(Long acceptedRecommendations) {
        this.acceptedRecommendations = acceptedRecommendations;
    }

    public Double getAcceptanceRate() {
        return this.acceptanceRate;
    }

    public void setAcceptanceRate(Double acceptanceRate) {
        this.acceptanceRate = acceptanceRate;
    }

}
