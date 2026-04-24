package com.company.itoms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("asset")
public class AssetEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("asset_code")
    private String assetCode;

    @TableField("asset_name")
    private String assetName;

    @TableField("age")
    private Double age;

    @TableField("total_life")
    private Double totalLife;

    @TableField("repair_count")
    private Integer repairCount;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted; // 0=未删, 1=已删

    /**
     * 计算资产健康度
     * health = (1 - (age/total_life)) * 0.6 + repair_count * 0.4
     */
    public Double calculateHealthStatus() {
        if (totalLife == null || totalLife == 0) {
            return 0.0;
        }
        return (1 - (age / totalLife)) * 0.6 + repairCount * 0.4;
    }
}
