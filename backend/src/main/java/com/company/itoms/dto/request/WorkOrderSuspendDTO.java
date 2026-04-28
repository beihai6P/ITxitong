package com.company.itoms.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WorkOrderSuspendDTO {
    @NotNull(message = "Work order ID cannot be null")
    private Long workOrderId;
    
    @NotNull(message = "Operator ID cannot be null")
    private Long operatorId;
    
    private String remark;

    public Long getWorkOrderId() {
        return this.workOrderId;
    }

    public void setWorkOrderId(Long workOrderId) {
        this.workOrderId = workOrderId;
    }

    public Long getOperatorId() {
        return this.operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public String getRemark() {
        return this.remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

}