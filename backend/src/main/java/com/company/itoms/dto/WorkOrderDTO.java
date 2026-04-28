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

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWorkOrderCode() {
        return this.workOrderCode;
    }

    public void setWorkOrderCode(String workOrderCode) {
        this.workOrderCode = workOrderCode;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getAssetId() {
        return this.assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public Integer getUrgencyLevel() {
        return this.urgencyLevel;
    }

    public void setUrgencyLevel(Integer urgencyLevel) {
        this.urgencyLevel = urgencyLevel;
    }

    public Integer getStatus() {
        return this.status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

}
