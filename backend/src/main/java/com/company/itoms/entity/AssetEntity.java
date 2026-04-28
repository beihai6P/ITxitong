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
    private Integer isDeleted;

    @TableField("brand")
    private String brand;

    @TableField("config")
    private String config;

    @TableField("department")
    private String department;

    @TableField("user")
    private String userName;

    @TableField("user_id")
    private Long userId;

    @TableField("department_id")
    private Long departmentId;

    @TableField("health_status")
    private String healthStatus;

    @TableField("asset_type")
    private String assetType;

    @TableField("asset_subtype")
    private String assetSubtype;

    @TableField("ip")
    private String ip;

    @TableField("purchase_date")
    private java.time.LocalDate purchaseDate;

    @TableField("status")
    private String status;

    @TableField("sn_code")
    private String snCode;

    @TableField("purchase_price")
    private java.math.BigDecimal purchasePrice;

    @TableField("warranty_expire_date")
    private java.time.LocalDate warrantyExpireDate;

    @TableField("asset_status")
    private String assetStatus;

    @TableField("location")
    private String location;

    @TableField("remark")
    private String remark;

    @TableField("qr_code_url")
    private String qrCodeUrl;

    @TableField("manager_name")
    private String managerName;

    @TableField("maintenance_contact")
    private String maintenanceContact;

    @TableField("last_inventory_time")
    private java.time.LocalDateTime lastInventoryTime;

    @TableField("depreciation_value")
    private java.math.BigDecimal depreciationValue;

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

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAssetCode() {
        return this.assetCode;
    }

    public void setAssetCode(String assetCode) {
        this.assetCode = assetCode;
    }

    public String getAssetName() {
        return this.assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public Double getAge() {
        return this.age;
    }

    public void setAge(Double age) {
        this.age = age;
    }

    public Double getTotalLife() {
        return this.totalLife;
    }

    public void setTotalLife(Double totalLife) {
        this.totalLife = totalLife;
    }

    public Integer getRepairCount() {
        return this.repairCount;
    }

    public void setRepairCount(Integer repairCount) {
        this.repairCount = repairCount;
    }

    public Integer getIsDeleted() {
        return this.isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }

    public String getBrand() {
        return this.brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getConfig() {
        return this.config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getDepartment() {
        return this.department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Long getUserId() { return this.userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public Long getDepartmentId() { return this.departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public String getHealthStatus() { return this.healthStatus; }
    public void setHealthStatus(String healthStatus) { this.healthStatus = healthStatus; }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAssetType() {
        return this.assetType;
    }

    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }

    public String getAssetSubtype() {
        return this.assetSubtype;
    }

    public void setAssetSubtype(String assetSubtype) {
        this.assetSubtype = assetSubtype;
    }

    public String getIp() {
        return this.ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public java.time.LocalDate getPurchaseDate() {
        return this.purchaseDate;
    }

    public void setPurchaseDate(java.time.LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSnCode() {
        return this.snCode;
    }

    public void setSnCode(String snCode) {
        this.snCode = snCode;
    }

    public java.math.BigDecimal getPurchasePrice() {
        return this.purchasePrice;
    }

    public void setPurchasePrice(java.math.BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public java.time.LocalDate getWarrantyExpireDate() {
        return this.warrantyExpireDate;
    }

    public void setWarrantyExpireDate(java.time.LocalDate warrantyExpireDate) {
        this.warrantyExpireDate = warrantyExpireDate;
    }

    public String getAssetStatus() {
        return this.assetStatus;
    }

    public void setAssetStatus(String assetStatus) {
        this.assetStatus = assetStatus;
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getRemark() {
        return this.remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getQrCodeUrl() {
        return this.qrCodeUrl;
    }

    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }

    public String getManagerName() {
        return this.managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public String getMaintenanceContact() {
        return this.maintenanceContact;
    }

    public void setMaintenanceContact(String maintenanceContact) {
        this.maintenanceContact = maintenanceContact;
    }

    public java.time.LocalDateTime getLastInventoryTime() {
        return this.lastInventoryTime;
    }

    public void setLastInventoryTime(java.time.LocalDateTime lastInventoryTime) {
        this.lastInventoryTime = lastInventoryTime;
    }

    public java.math.BigDecimal getDepreciationValue() {
        return this.depreciationValue;
    }

    public void setDepreciationValue(java.math.BigDecimal depreciationValue) {
        this.depreciationValue = depreciationValue;
    }

}
