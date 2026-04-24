package com.company.itoms.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WorkOrderFinishDTO {
    @NotNull(message = "Work order ID cannot be null")
    private Long workOrderId;
    
    private Long operatorId;
    
    private String remark;
}
