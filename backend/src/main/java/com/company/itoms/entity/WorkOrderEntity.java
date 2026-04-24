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

    @TableField(exist = false)
    private AssetEntity asset;
}
