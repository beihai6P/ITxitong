package com.company.itoms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("sys_dict")
public class SysDictEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String dictCode;
    private String dictName;
    private String dictValue;
    private Integer sortOrder;
    private Integer isActive;
}
