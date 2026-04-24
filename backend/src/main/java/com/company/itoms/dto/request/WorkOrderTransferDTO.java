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
}