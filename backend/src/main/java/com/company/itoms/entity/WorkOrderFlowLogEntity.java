package com.company.itoms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("work_order_flow_log")
public class WorkOrderFlowLogEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long workOrderId;

    private Long operatorId;

    private String action;

    private Integer previousStatus;

    private Integer currentStatus;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}