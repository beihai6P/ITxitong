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

    public Long getWorkOrderId() {
        return this.workOrderId;
    }

    public void setWorkOrderId(Long workOrderId) {
        this.workOrderId = workOrderId;
    }

    public Integer getEvaluateScore() {
        return this.evaluateScore;
    }

    public void setEvaluateScore(Integer evaluateScore) {
        this.evaluateScore = evaluateScore;
    }

    public String getEvaluateRemark() {
        return this.evaluateRemark;
    }

    public void setEvaluateRemark(String evaluateRemark) {
        this.evaluateRemark = evaluateRemark;
    }

    public Long getOperatorId() {
        return this.operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

}
