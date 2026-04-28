package com.company.itoms.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WorkOrderTransferDTO {
    @NotNull(message = "Work order ID cannot be null")
    private Long workOrderId;
    
    @NotNull(message = "Target assignee ID cannot be null")
    private Long targetAssigneeId;
    
    @NotNull(message = "Operator ID cannot be null")
    private Long operatorId;
    
    private String remark;

    public Long getWorkOrderId() {
        return this.workOrderId;
    }

    public void setWorkOrderId(Long workOrderId) {
        this.workOrderId = workOrderId;
    }

    public Long getTargetAssigneeId() {
        return this.targetAssigneeId;
    }

    public void setTargetAssigneeId(Long targetAssigneeId) {
        this.targetAssigneeId = targetAssigneeId;
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