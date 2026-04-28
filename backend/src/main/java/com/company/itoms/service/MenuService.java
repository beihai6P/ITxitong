package com.company.itoms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.company.itoms.entity.MenuEntity;
import java.util.List;

public interface MenuService extends IService<MenuEntity> {

    List<MenuEntity> getMenuTree();

    List<MenuEntity> getMenusByRoleId(Long roleId);

    List<MenuEntity> getEnabledMenus();

    List<String> getPermissionsByUserId(Long userId);

    List<MenuEntity> buildMenuTree(List<MenuEntity> menus);

    List<MenuEntity> getUserMenus(Long userId);
}
