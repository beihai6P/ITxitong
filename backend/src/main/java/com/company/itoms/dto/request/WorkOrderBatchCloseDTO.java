package com.company.itoms.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class WorkOrderBatchCloseDTO {
    @NotEmpty(message = "Work order IDs cannot be empty")
    private List<Long> workOrderIds;
    
    private Long operatorId;
    
    private String remark;
}
