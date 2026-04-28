package com.company.itoms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("work_order_dispatch_log")
public class WorkOrderDispatchLogEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("order_id")
    private Long orderId;

    @TableField("dispatch_engineer_id")
    private Long dispatchEngineerId;

    @TableField("accept_engineer_id")
    private Long acceptEngineerId;

    @TableField("dispatch_time")
    private java.time.LocalDateTime dispatchTime;

    @TableField("accept_time")
    private java.time.LocalDateTime acceptTime;

    @TableField("refuse_reason")
    private String refuseReason;

    @TableField("status")
    private String status;

    @TableField("create_time")
    private java.time.LocalDateTime createTime;

    @TableField(exist = false)
    private String dispatchEngineerName;

    @TableField(exist = false)
    private String acceptEngineerName;

    @TableField(exist = false)
    private String orderCode;
}
