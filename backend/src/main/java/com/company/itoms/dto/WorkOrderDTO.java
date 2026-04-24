package com.company.itoms.dto;

import lombok.Data;

@Data
public class WorkOrderDTO {
    private Long id;
    private String workOrderCode;
    private String description;
    private Long assetId;
    private Integer urgencyLevel;
    private Integer status;
}
