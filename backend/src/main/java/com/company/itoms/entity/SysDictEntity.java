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

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDictCode() {
        return this.dictCode;
    }

    public void setDictCode(String dictCode) {
        this.dictCode = dictCode;
    }

    public String getDictName() {
        return this.dictName;
    }

    public void setDictName(String dictName) {
        this.dictName = dictName;
    }

    public String getDictValue() {
        return this.dictValue;
    }

    public void setDictValue(String dictValue) {
        this.dictValue = dictValue;
    }

    public Integer getSortOrder() {
        return this.sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getIsActive() {
        return this.isActive;
    }

    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
    }

}
