package com.company.itoms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WorkOrderCreateDTO {
    private String workOrderCode;
    
    private Integer faultType;

    @NotBlank(message = "Description cannot be blank")
    private String description;
    
    private Long assetId;
    
    private Long creatorId;
    
    private Long departmentId;

    private Integer urgencyLevel;
    
    private String contactPhone;
}
