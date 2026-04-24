package com.company.itoms.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WorkOrderEvaluateDTO {
    @NotNull(message = "Work order ID cannot be null")
    private Long workOrderId;
    
    @NotNull(message = "Score cannot be null")
    private Integer evaluateScore;
    
    private String evaluateRemark;
    
    private Long operatorId;
}
