package com.company.itoms.dto.response;

import lombok.Data;

@Data
public class AiPredictionDTO {
    private String predictionResult;
    private Double confidence;
    private String details;
    private String source;
    private String rawAiResponse;
}