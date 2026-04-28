package com.company.itoms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("asset_operate_log")
public class AssetLifecycleLogEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("asset_id")
    private Long assetId;

    @TableField("operate_type")
    private String operateType;

    @TableField("operator_id")
    private Long operatorId;

    @TableField("operator_name")
    private String operatorName;

    @TableField("operate_time")
    private java.time.LocalDateTime operateTime;

    @TableField("detail")
    private String detail;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAssetId() {
        return this.assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public String getOperateType() {
        return this.operateType;
    }

    public void setOperateType(String operateType) {
        this.operateType = operateType;
    }

    public Long getOperatorId() {
        return this.operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public String getOperatorName() {
        return this.operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public java.time.LocalDateTime getOperateTime() {
        return this.operateTime;
    }

    public void setOperateTime(java.time.LocalDateTime operateTime) {
        this.operateTime = operateTime;
    }

    public String getDetail() {
        return this.detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

}