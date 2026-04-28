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

    public List<Long> getWorkOrderIds() {
        return this.workOrderIds;
    }

    public void setWorkOrderIds(List<Long> workOrderIds) {
        this.workOrderIds = workOrderIds;
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
