package com.company.itoms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.company.itoms.entity.MenuEntity;
import com.company.itoms.entity.RoleEntity;
import java.util.List;

public interface RoleService extends IService<RoleEntity> {

    Page<RoleEntity> getPage(Page<RoleEntity> page, String roleName, String roleCode, Integer status);

    List<RoleEntity> getAllEnabledRoles();

    List<MenuEntity> getMenusByRoleId(Long roleId);

    void saveRoleWithMenus(RoleEntity role);

    void updateRoleWithMenus(RoleEntity role);

    void deleteRoleWithMenus(Long roleId);

    boolean hasRoleWithCode(String roleCode, Long excludeId);

    List<Long> getMenuIdsByRoleId(Long roleId);
}
