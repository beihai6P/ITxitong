package com.company.itoms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("engineer_work_stat")
public class EngineerWorkStatEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("engineer_id")
    private Long engineerId;

    @TableField("engineer_name")
    private String engineerName;

    @TableField("total_order")
    private Integer totalOrder;

    @TableField("accept_order")
    private Integer acceptOrder;

    @TableField("finish_order")
    private Integer finishOrder;

    @TableField("finish_rate")
    private java.math.BigDecimal finishRate;

    @TableField("avg_process_time")
    private Integer avgProcessTime;

    @TableField("stat_date")
    private java.time.LocalDate statDate;

    @TableField("create_time")
    private java.time.LocalDateTime createTime;
}
