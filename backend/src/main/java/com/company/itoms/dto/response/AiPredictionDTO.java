package com.company.itoms.dto.response;

import lombok.Data;

@Data
public class AiPredictionDTO {
    private String predictionResult;
    private Double confidence;
    private String details;
    private String source;
    private String rawAiResponse;

    public String getPredictionResult() {
        return this.predictionResult;
    }

    public void setPredictionResult(String predictionResult) {
        this.predictionResult = predictionResult;
    }

    public Double getConfidence() {
        return this.confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public String getDetails() {
        return this.details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getSource() {
        return this.source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getRawAiResponse() {
        return this.rawAiResponse;
    }

    public void setRawAiResponse(String rawAiResponse) {
        this.rawAiResponse = rawAiResponse;
    }

}