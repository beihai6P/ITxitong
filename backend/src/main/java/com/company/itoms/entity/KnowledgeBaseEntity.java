package com.company.itoms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("knowledge_base")
public class KnowledgeBaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField("fault_type")
    private String faultType;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
