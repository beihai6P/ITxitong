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

    public List<Long> getWorkOrderIds() {
        return this.workOrderIds;
    }

    public void setWorkOrderIds(List<Long> workOrderIds) {
        this.workOrderIds = workOrderIds;
    }

    public Long getAssigneeId() {
        return this.assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
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
