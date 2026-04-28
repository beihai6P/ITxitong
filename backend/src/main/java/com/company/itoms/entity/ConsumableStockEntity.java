package com.company.itoms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("consumable_stock")
public class ConsumableStockEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer stockQuantity;
    private Integer safeThreshold;
}
