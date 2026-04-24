package com.company.itoms.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class WorkOrderBatchAssignDTO {
    @NotEmpty(message = "Work order IDs cannot be empty")
    private List<Long> workOrderIds;
    
    @NotNull(message = "Assignee ID cannot be null")
    private Long assigneeId;
    
    private Long operatorId;
    
    private String remark;
}
