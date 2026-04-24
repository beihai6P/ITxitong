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
}
