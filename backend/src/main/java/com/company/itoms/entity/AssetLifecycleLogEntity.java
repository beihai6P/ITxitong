package com.company.itoms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("asset_lifecycle_log")
public class AssetLifecycleLogEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("asset_id")
    private Long assetId;

    @TableField("action")
    private String action; // e.g. PURCHASE, ASSIGN, REPAIR, SCRAP

    @TableField("operator_id")
    private Long operatorId;

    @TableField("remark")
    private String remark;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}