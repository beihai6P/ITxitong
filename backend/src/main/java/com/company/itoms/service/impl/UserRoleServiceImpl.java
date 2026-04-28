package com.company.itoms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.itoms.entity.MenuEntity;
import com.company.itoms.entity.RoleEntity;
import com.company.itoms.entity.UserRoleEntity;
import com.company.itoms.mapper.UserRoleMapper;
import com.company.itoms.service.MenuService;
import com.company.itoms.service.RoleService;
import com.company.itoms.service.UserRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRoleEntity> implements UserRoleService {

    private final MenuService menuService;
    private final RoleService roleService;

    @Override
    public List<Long> getRoleIdsByUserId(Long userId) {
        return baseMapper.selectRoleIdsByUserId(userId);
    }

    @Override
    public List<String> getRoleCodesByUserId(Long userId) {
        List<Long> roleIds = getRoleIdsByUserId(userId);
        if (roleIds == null || roleIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<RoleEntity> roles = roleService.listByIds(roleIds);
        return roles.stream()
                .map(RoleEntity::getRoleCode)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> getMenuIdsByRoleId(Long roleId) {
        return baseMapper.selectMenuIdsByRoleId(roleId);
    }

    @Override
    public List<String> getPermissionsByUserId(Long userId) {
        List<Long> roleIds = getRoleIdsByUserId(userId);
        if (roleIds == null || roleIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> allMenuIds = new ArrayList<>();
        for (Long roleId : roleIds) {
            List<Long> menuIds = getMenuIdsByRoleId(roleId);
            if (menuIds != null) {
                allMenuIds.addAll(menuIds);
            }
        }

        if (allMenuIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<MenuEntity> menus = menuService.listByIds(allMenuIds);
        return menus.stream()
                .map(MenuEntity::getPerms)
                .filter(perms -> perms != null && !perms.isEmpty())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        baseMapper.deleteByUserId(userId);
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                UserRoleEntity ur = new UserRoleEntity();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                baseMapper.insert(ur);
            }
        }
    }

    @Override
    @Transactional
    public void removeAllRoles(Long userId) {
        baseMapper.deleteByUserId(userId);
    }

    @Override
    @Transactional
    public void bindMenuToRole(Long roleId, List<Long> menuIds) {
    }

    @Override
    @Transactional
    public void removeMenuFromRole(Long roleId) {
        baseMapper.deleteByRoleId(roleId);
    }
}
