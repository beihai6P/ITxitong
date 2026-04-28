package com.company.itoms.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class AiRecommendationDTO {
    private Long solutionId;
    private Double confidence; // [0.0~1.0]
    private String source; // "KNOWLEDGE_BASE" / "HISTORICAL_DATA" / "AI_GENERATED"
    private String content;

    @JsonIgnore
    private String rawAiResponse;

    public Long getSolutionId() {
        return this.solutionId;
    }

    public void setSolutionId(Long solutionId) {
        this.solutionId = solutionId;
    }

    public Double getConfidence() {
        return this.confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public String getSource() {
        return this.source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRawAiResponse() {
        return this.rawAiResponse;
    }

    public void setRawAiResponse(String rawAiResponse) {
        this.rawAiResponse = rawAiResponse;
    }

}
