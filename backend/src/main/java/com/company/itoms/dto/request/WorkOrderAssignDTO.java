package com.company.itoms.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WorkOrderAssignDTO {
    @NotNull(message = "Work order ID cannot be null")
    private Long workOrderId;
    
    @NotNull(message = "Assignee ID cannot be null")
    private Long assigneeId;
    
    private Long operatorId;
    
    private String remark;
}
