package com.company.itoms.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.company.itoms.common.Sensitive;
import com.company.itoms.common.SensitiveType;
import lombok.Data;

@Data
@TableName("work_order")
public class WorkOrderEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("work_order_code")
    private String workOrderCode;

    @TableField("description")
    private String description;

    @TableField("fault_type")
    private String faultType;

    @TableField("asset_id")
    private Long assetId;

    @TableField("creator_id")
    private Long creatorId;

    @TableField("department_id")
    private Long departmentId;

    @TableField("assignee_id")
    private Long assigneeId;

    @TableField("evaluate_score")
    private Integer evaluateScore;

    @TableField("evaluate_remark")
    private String evaluateRemark;

    @TableField("urgency_level")
    private Integer urgencyLevel;

    @TableField("status")
    private Integer status;

    @TableField("contact_phone")
    @Sensitive(type = SensitiveType.PHONE)
    private String contactPhone;

    @TableField("priority_level")
    private Integer priorityLevel;

    @TableField("sla_deadline")
    private java.time.LocalDateTime slaDeadline;

    @TableField("create_time")
    private java.time.LocalDateTime createTime;

    @TableField("update_time")
    private java.time.LocalDateTime updateTime;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

    @TableField("dispatch_engineer_id")
    private Long dispatchEngineerId;

    @TableField("accept_engineer_id")
    private Long acceptEngineerId;

    @TableField("dispatch_time")
    private java.time.LocalDateTime dispatchTime;

    @TableField("accept_time")
    private java.time.LocalDateTime acceptTime;

    @TableField("process_progress")
    private Integer processProgress;

    @TableField("timeout_minutes")
    private Integer timeoutMinutes;

    @TableField(exist = false)
    private AssetEntity asset;

    @TableField(exist = false)
    private String creatorName;

    @TableField(exist = false)
    private String departmentName;

    @TableField(exist = false)
    private String assigneeName;

    @TableField(exist = false)
    private String dispatchEngineerName;

    @TableField(exist = false)
    private String acceptEngineerName;

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

    public String getFaultType() {
        return this.faultType;
    }

    public void setFaultType(String faultType) {
        this.faultType = faultType;
    }

    public Long getAssetId() {
        return this.assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public Long getCreatorId() {
        return this.creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public Long getDepartmentId() {
        return this.departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public Long getAssigneeId() {
        return this.assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }

    public Integer getEvaluateScore() {
        return this.evaluateScore;
    }

    public void setEvaluateScore(Integer evaluateScore) {
        this.evaluateScore = evaluateScore;
    }

    public String getEvaluateRemark() {
        return this.evaluateRemark;
    }

    public void setEvaluateRemark(String evaluateRemark) {
        this.evaluateRemark = evaluateRemark;
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

    public String getContactPhone() {
        return this.contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public Integer getPriorityLevel() {
        return this.priorityLevel;
    }

    public void setPriorityLevel(Integer priorityLevel) {
        this.priorityLevel = priorityLevel;
    }

    public Integer getIsDeleted() {
        return this.isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Long getDispatchEngineerId() {
        return dispatchEngineerId;
    }

    public void setDispatchEngineerId(Long dispatchEngineerId) {
        this.dispatchEngineerId = dispatchEngineerId;
    }

    public Long getAcceptEngineerId() {
        return acceptEngineerId;
    }

    public void setAcceptEngineerId(Long acceptEngineerId) {
        this.acceptEngineerId = acceptEngineerId;
    }

    public java.time.LocalDateTime getDispatchTime() {
        return dispatchTime;
    }

    public void setDispatchTime(java.time.LocalDateTime dispatchTime) {
        this.dispatchTime = dispatchTime;
    }

    public java.time.LocalDateTime getAcceptTime() {
        return acceptTime;
    }

    public void setAcceptTime(java.time.LocalDateTime acceptTime) {
        this.acceptTime = acceptTime;
    }

    public Integer getProcessProgress() {
        return processProgress;
    }

    public void setProcessProgress(Integer processProgress) {
        this.processProgress = processProgress;
    }

    public Integer getTimeoutMinutes() {
        return timeoutMinutes;
    }

    public void setTimeoutMinutes(Integer timeoutMinutes) {
        this.timeoutMinutes = timeoutMinutes;
    }

    public AssetEntity getAsset() {
        return this.asset;
    }

    public void setAsset(AssetEntity asset) {
        this.asset = asset;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public void setAssigneeName(String assigneeName) {
        this.assigneeName = assigneeName;
    }

    public String getDispatchEngineerName() {
        return dispatchEngineerName;
    }

    public void setDispatchEngineerName(String dispatchEngineerName) {
        this.dispatchEngineerName = dispatchEngineerName;
    }

    public String getAcceptEngineerName() {
        return acceptEngineerName;
    }

    public void setAcceptEngineerName(String acceptEngineerName) {
        this.acceptEngineerName = acceptEngineerName;
    }

}
