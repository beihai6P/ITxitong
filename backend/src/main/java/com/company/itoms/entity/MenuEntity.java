package com.company.itoms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("menu")
public class MenuEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long parentId;

    private String menuName;

    private String path;

    private String component;

    private String icon;

    private Integer sortOrder;

    private String menuType;

    private String perms;

    private Integer isExternal;

    private Integer isHidden;

    private Integer status;

    private String visible;

    private String keepAlive;

    private String alwaysShow;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;

    @TableField(exist = false)
    private Boolean checked;

    @TableField(exist = false)
    private Integer level;

    @TableField(exist = false)
    private String title;

    @TableField(exist = false)
    private List<MenuEntity> children;

    public List<MenuEntity> getChildren() {
        return this.children;
    }

    public void setChildren(List<MenuEntity> children) {
        this.children = children;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return this.parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getMenuName() {
        return this.menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getComponent() {
        return this.component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getIcon() {
        return this.icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getSortOrder() {
        return this.sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getMenuType() {
        return this.menuType;
    }

    public void setMenuType(String menuType) {
        this.menuType = menuType;
    }

    public String getPerms() {
        return this.perms;
    }

    public void setPerms(String perms) {
        this.perms = perms;
    }

    public Integer getIsExternal() {
        return this.isExternal;
    }

    public void setIsExternal(Integer isExternal) {
        this.isExternal = isExternal;
    }

    public Integer getIsHidden() {
        return this.isHidden;
    }

    public void setIsHidden(Integer isHidden) {
        this.isHidden = isHidden;
    }

    public Integer getStatus() {
        return this.status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getVisible() {
        return this.visible;
    }

    public void setVisible(String visible) {
        this.visible = visible;
    }

    public String getKeepAlive() {
        return this.keepAlive;
    }

    public void setKeepAlive(String keepAlive) {
        this.keepAlive = keepAlive;
    }

    public String getAlwaysShow() {
        return this.alwaysShow;
    }

    public void setAlwaysShow(String alwaysShow) {
        this.alwaysShow = alwaysShow;
    }

    public LocalDateTime getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return this.updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getIsDeleted() {
        return this.isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Boolean getChecked() {
        return this.checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }

    public Integer getLevel() {
        return this.level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
