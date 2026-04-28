package com.company.itoms.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class AiRecommendationDTO {
    private Long solutionId;
    private Double confidence; // 0.0~1.0
    private String source; // "KNOWLEDGE_BASE" / "HISTORICAL_DATA"
    
    @JsonIgnore
    private String internalDebugInfo;

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

    public String getInternalDebugInfo() {
        return this.internalDebugInfo;
    }

    public void setInternalDebugInfo(String internalDebugInfo) {
        this.internalDebugInfo = internalDebugInfo;
    }

}
