package com.company.itoms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.itoms.common.Result;
import com.company.itoms.entity.MenuEntity;
import com.company.itoms.entity.RoleEntity;
import com.company.itoms.service.MenuService;
import com.company.itoms.service.RoleMenuService;
import com.company.itoms.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "角色管理")
@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;
    private final MenuService menuService;
    private final RoleMenuService roleMenuService;

    @Operation(summary = "获取所有角色(分页)")
    @GetMapping("/list")
    public Result<Page<RoleEntity>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) String roleCode,
            @RequestParam(required = false) Integer status) {

        Page<RoleEntity> page = new Page<>(current, size);
        return Result.success(roleService.getPage(page, roleName, roleCode, status));
    }

    @Operation(summary = "获取所有启用角色")
    @GetMapping("/all")
    public Result<List<RoleEntity>> getAllEnabled() {
        return Result.success(roleService.getAllEnabledRoles());
    }

    @Operation(summary = "根据ID获取角色")
    @GetMapping("/{id}")
    public Result<RoleEntity> getById(@PathVariable Long id) {
        RoleEntity role = roleService.getById(id);
        if (role != null) {
            List<Long> menuIds = roleService.getMenuIdsByRoleId(id);
            role.setMenuIds(menuIds);
        }
        return Result.success(role);
    }

    @Operation(summary = "新增角色")
    @PostMapping
    public Result<Boolean> save(@RequestBody RoleEntity role) {
        if (StringUtils.hasText(role.getRoleCode())) {
            if (roleService.hasRoleWithCode(role.getRoleCode(), null)) {
                return Result.error(400, "角色编码已存在");
            }
        }
        if (role.getStatus() == null) {
            role.setStatus(1);
        }
        roleService.saveRoleWithMenus(role);
        return Result.success(true);
    }

    @Operation(summary = "修改角色")
    @PutMapping
    public Result<Boolean> update(@RequestBody RoleEntity role) {
        if (role.getId() == null) {
            return Result.error(400, "角色ID不能为空");
        }
        if (StringUtils.hasText(role.getRoleCode())) {
            if (roleService.hasRoleWithCode(role.getRoleCode(), role.getId())) {
                return Result.error(400, "角色编码已存在");
            }
        }
        roleService.updateRoleWithMenus(role);
        return Result.success(true);
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    public Result<Boolean> remove(@PathVariable Long id) {
        if (id == 1) {
            return Result.error(403, "超级管理员角色不能删除");
        }
        roleService.deleteRoleWithMenus(id);
        return Result.success(true);
    }

    @Operation(summary = "修改角色状态")
    @PutMapping("/{id}/status")
    public Result<Boolean> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        RoleEntity role = roleService.getById(id);
        if (role == null) {
            return Result.error(404, "角色不存在");
        }
        if (id == 1) {
            return Result.error(403, "超级管理员角色不能禁用");
        }
        role.setStatus(status);
        roleService.updateById(role);
        return Result.success(true);
    }

    @Operation(summary = "获取角色菜单权限树")
    @GetMapping("/{id}/menus")
    public Result<List<MenuEntity>> getRoleMenus(@PathVariable Long id) {
        List<MenuEntity> allMenus = menuService.getMenuTree();
        List<Long> checkedMenuIds = roleService.getMenuIdsByRoleId(id);

        markCheckedMenus(allMenus, checkedMenuIds);
        return Result.success(allMenus);
    }

    @Operation(summary = "分配角色菜单权限")
    @PutMapping("/{id}/menus")
    public Result<Boolean> assignMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
        RoleEntity role = roleService.getById(id);
        if (role == null) {
            return Result.error(404, "角色不存在");
        }
        roleMenuService.saveRoleMenus(id, menuIds);
        return Result.success(true);
    }

    private void markCheckedMenus(List<MenuEntity> menus, List<Long> checkedMenuIds) {
        if (menus == null) return;
        for (MenuEntity menu : menus) {
            if (checkedMenuIds != null && checkedMenuIds.contains(menu.getId())) {
                menu.setChecked(true);
            }
            if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
                markCheckedMenus(menu.getChildren(), checkedMenuIds);
            }
        }
    }
}
