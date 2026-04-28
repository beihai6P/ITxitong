package com.company.itoms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.itoms.entity.MenuEntity;
import com.company.itoms.entity.RoleEntity;
import com.company.itoms.entity.RoleMenuEntity;
import com.company.itoms.mapper.MenuMapper;
import com.company.itoms.mapper.RoleMapper;
import com.company.itoms.mapper.RoleMenuMapper;
import com.company.itoms.service.RoleMenuService;
import com.company.itoms.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, RoleEntity> implements RoleService {

    private final RoleMenuMapper roleMenuMapper;
    private final MenuMapper menuMapper;
    private final RoleMenuService roleMenuService;

    @Override
    public Page<RoleEntity> getPage(Page<RoleEntity> page, String roleName, String roleCode, Integer status) {
        LambdaQueryWrapper<RoleEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(roleName)) {
            wrapper.like(RoleEntity::getRoleName, roleName);
        }
        if (StringUtils.hasText(roleCode)) {
            wrapper.eq(RoleEntity::getRoleCode, roleCode);
        }
        if (status != null) {
            wrapper.eq(RoleEntity::getStatus, status);
        }
        wrapper.orderByAsc(RoleEntity::getRoleSort);
        return page(page, wrapper);
    }

    @Override
    public List<RoleEntity> getAllEnabledRoles() {
        return list(new LambdaQueryWrapper<RoleEntity>()
                .eq(RoleEntity::getStatus, 1)
                .orderByAsc(RoleEntity::getRoleSort));
    }

    @Override
    public List<MenuEntity> getMenusByRoleId(Long roleId) {
        List<Long> menuIds = roleMenuMapper.selectMenuIdsByRoleId(roleId);
        if (menuIds == null || menuIds.isEmpty()) {
            return new ArrayList<>();
        }
        return menuMapper.selectBatchIds(menuIds);
    }

    @Override
    @Transactional
    public void saveRoleWithMenus(RoleEntity role) {
        role.setIsDeleted(0);
        role.setRoleSort(role.getRoleSort() == null ? 0 : role.getRoleSort());
        save(role);
        if (role.getMenuIds() != null && !role.getMenuIds().isEmpty()) {
            roleMenuService.saveRoleMenus(role.getId(), role.getMenuIds());
        }
    }

    @Override
    @Transactional
    public void updateRoleWithMenus(RoleEntity role) {
        updateById(role);
        roleMenuService.deleteByRoleId(role.getId());
        if (role.getMenuIds() != null && !role.getMenuIds().isEmpty()) {
            roleMenuService.saveRoleMenus(role.getId(), role.getMenuIds());
        }
    }

    @Override
    @Transactional
    public void deleteRoleWithMenus(Long roleId) {
        roleMenuService.deleteByRoleId(roleId);
        removeById(roleId);
    }

    @Override
    public boolean hasRoleWithCode(String roleCode, Long excludeId) {
        LambdaQueryWrapper<RoleEntity> wrapper = new LambdaQueryWrapper<RoleEntity>()
                .eq(RoleEntity::getRoleCode, roleCode);
        if (excludeId != null) {
            wrapper.ne(RoleEntity::getId, excludeId);
        }
        return count(wrapper) > 0;
    }

    @Override
    public List<Long> getMenuIdsByRoleId(Long roleId) {
        return roleMenuMapper.selectMenuIdsByRoleId(roleId);
    }
}
