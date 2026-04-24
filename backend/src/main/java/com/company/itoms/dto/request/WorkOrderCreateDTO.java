package com.company.itoms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WorkOrderCreateDTO {
    @NotBlank(message = "Work order code cannot be blank")
    private String workOrderCode;
    
    @NotBlank(message = "Fault type cannot be blank")
    private String faultType;

    @NotBlank(message = "Description cannot be blank")
    private String description;
    
    private Long assetId;
    
    private Long creatorId;
    
    private Long departmentId;

    private Integer urgencyLevel;
}
