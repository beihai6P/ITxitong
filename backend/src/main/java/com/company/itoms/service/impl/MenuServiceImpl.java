package com.company.itoms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.itoms.entity.MenuEntity;
import com.company.itoms.entity.UserEntity;
import com.company.itoms.mapper.MenuMapper;
import com.company.itoms.mapper.UserMapper;
import com.company.itoms.mapper.UserRoleMapper;
import com.company.itoms.service.MenuService;
import com.company.itoms.service.RoleMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl extends ServiceImpl<MenuMapper, MenuEntity> implements MenuService {

    private final RoleMenuService roleMenuService;
    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;

    @Override
    public List<MenuEntity> getMenuTree() {
        List<MenuEntity> allMenus = list(new LambdaQueryWrapper<MenuEntity>()
                .eq(MenuEntity::getStatus, 1)
                .orderByAsc(MenuEntity::getSortOrder));
        return buildMenuTree(allMenus);
    }

    @Override
    public List<MenuEntity> getMenusByRoleId(Long roleId) {
        List<Long> menuIds = roleMenuService.getMenuIdsByRoleId(roleId);
        if (menuIds == null || menuIds.isEmpty()) {
            return new ArrayList<>();
        }
        return listByIds(menuIds);
    }

    @Override
    public List<MenuEntity> getEnabledMenus() {
        return list(new LambdaQueryWrapper<MenuEntity>()
                .eq(MenuEntity::getStatus, 1)
                .orderByAsc(MenuEntity::getSortOrder));
    }

    @Override
    public List<String> getPermissionsByUserId(Long userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            return new ArrayList<>();
        }

        if ("admin".equalsIgnoreCase(user.getUsername())) {
            List<MenuEntity> allMenus = list(new LambdaQueryWrapper<MenuEntity>()
                    .eq(MenuEntity::getStatus, 1)
                    .isNotNull(MenuEntity::getPerms));
            List<String> perms = allMenus.stream()
                    .map(MenuEntity::getPerms)
                    .filter(p -> p != null && !p.isEmpty())
                    .collect(Collectors.toList());
            perms.add("*:*:*");
            return perms;
        }

        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);
        if (roleIds == null || roleIds.isEmpty()) {
            String legacyRole = user.getRole();
            if (legacyRole != null && !legacyRole.isEmpty()) {
                List<MenuEntity> roleMenus = list(new LambdaQueryWrapper<MenuEntity>()
                        .eq(MenuEntity::getStatus, 1)
                        .isNotNull(MenuEntity::getPerms));
                return roleMenus.stream()
                        .map(MenuEntity::getPerms)
                        .filter(p -> p != null && !p.isEmpty())
                        .collect(Collectors.toList());
            }
            return new ArrayList<>();
        }

        Set<Long> menuIdSet = new HashSet<>();
        for (Long roleId : roleIds) {
            List<Long> menuIds = roleMenuService.getMenuIdsByRoleId(roleId);
            if (menuIds != null) {
                menuIdSet.addAll(menuIds);
            }
        }

        if (menuIdSet.isEmpty()) {
            return new ArrayList<>();
        }

        List<MenuEntity> menus = list(new LambdaQueryWrapper<MenuEntity>()
                .in(MenuEntity::getId, menuIdSet)
                .eq(MenuEntity::getStatus, 1)
                .isNotNull(MenuEntity::getPerms));

        return menus.stream()
                .map(MenuEntity::getPerms)
                .filter(p -> p != null && !p.isEmpty())
                .collect(Collectors.toList());
    }

    @Override
    public List<MenuEntity> buildMenuTree(List<MenuEntity> menus) {
        Map<Long, List<MenuEntity>> parentIdMap = menus.stream()
                .collect(Collectors.groupingBy(MenuEntity::getParentId));

        List<MenuEntity> rootMenus = parentIdMap.getOrDefault(0L, new ArrayList<>());
        rootMenus.forEach(menu -> buildChildren(menu, parentIdMap));

        return rootMenus;
    }

    private void buildChildren(MenuEntity parent, Map<Long, List<MenuEntity>> parentIdMap) {
        List<MenuEntity> children = parentIdMap.getOrDefault(parent.getId(), new ArrayList<>());
        children.forEach(child -> buildChildren(child, parentIdMap));
        parent.setChildren(children);
    }

    @Override
    public List<MenuEntity> getUserMenus(Long userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            return new ArrayList<>();
        }

        List<MenuEntity> userMenus;

        if ("admin".equalsIgnoreCase(user.getUsername())) {
            userMenus = getEnabledMenus();
        } else {
            List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);

            if (roleIds == null || roleIds.isEmpty()) {
                String legacyRole = user.getRole();
                if (legacyRole != null && !legacyRole.isEmpty()) {
                    userMenus = getEnabledMenus();
                } else {
                    return new ArrayList<>();
                }
            } else {
                Set<Long> menuIdSet = new HashSet<>();
                for (Long roleId : roleIds) {
                    List<Long> menuIds = roleMenuService.getMenuIdsByRoleId(roleId);
                    if (menuIds != null) {
                        menuIdSet.addAll(menuIds);
                    }
                }

                if (menuIdSet.isEmpty()) {
                    return new ArrayList<>();
                }

                userMenus = list(new LambdaQueryWrapper<MenuEntity>()
                        .in(MenuEntity::getId, menuIdSet)
                        .eq(MenuEntity::getStatus, 1)
                        .orderByAsc(MenuEntity::getSortOrder));
            }
        }

        return buildMenuTree(userMenus);
    }
}
