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

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFaultType() {
        return this.faultType;
    }

    public void setFaultType(String faultType) {
        this.faultType = faultType;
    }

    public Integer getIsDeleted() {
        return this.isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }

}
