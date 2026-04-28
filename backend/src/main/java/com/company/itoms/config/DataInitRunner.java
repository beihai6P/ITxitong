package com.company.itoms.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.itoms.entity.MenuEntity;
import com.company.itoms.entity.RoleEntity;
import com.company.itoms.entity.RoleMenuEntity;
import com.company.itoms.mapper.MenuMapper;
import com.company.itoms.mapper.RoleMapper;
import com.company.itoms.mapper.RoleMenuMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitRunner implements CommandLineRunner {

    private final RoleMapper roleMapper;
    private final MenuMapper menuMapper;
    private final RoleMenuMapper roleMenuMapper;

    @Override
    @Transactional
    public void run(String... args) {
        initializeRoles();
        initializeMenus();
        initializeRoleMenus();
    }

    private void initializeRoles() {
        log.info("========== 开始初始化预设角色 ==========");

        createRoleIfNotExists("SUPER_ADMIN", "超级管理员", "拥有系统全部权限，不受任何限制", 1, 1, "ALL");
        createRoleIfNotExists("ADMIN", "行政/管理岗", "人员管理、资产台账查看、报表统计权限", 1, 2, "ALL");
        createRoleIfNotExists("ENGINEER", "运维工程师", "工单接单、设备维修、故障处理权限", 1, 3, "SELF");
        createRoleIfNotExists("USER", "普通员工", "仅自助报修、查看本人工单权限", 1, 4, "SELF");

        log.info("========== 预设角色初始化完成 ==========");
    }

    private void createRoleIfNotExists(String roleCode, String roleName, String description,
                                       Integer status, Integer roleSort, String dataScope) {
        RoleEntity existRole = roleMapper.selectOne(new LambdaQueryWrapper<RoleEntity>()
                .eq(RoleEntity::getRoleCode, roleCode));

        if (existRole == null) {
            RoleEntity role = new RoleEntity();
            role.setRoleCode(roleCode);
            role.setRoleName(roleName);
            role.setDescription(description);
            role.setStatus(status);
            role.setRoleSort(roleSort);
            role.setDataScope(dataScope);
            role.setIsDeleted(0);
            roleMapper.insert(role);
            log.info("Created role: {} - {}", roleCode, roleName);
        } else {
            log.info("Role already exists: {} - {}", roleCode, roleName);
        }
    }

    private void initializeMenus() {
        log.info("========== 开始初始化预设菜单 ==========");

        createMenuIfNotExists(1L, 0L, "监控大屏", "/dashboard", "dashboard/index", "Odometer", 1, "C", "dashboard:view");
        createMenuIfNotExists(2L, 0L, "资产管理", "/asset", "asset/index", "Monitor", 2, "C", "asset:view");
        createMenuIfNotExists(3L, 0L, "工单管理", "/work-order", "work-order/index", "Document", 3, "C", "workorder:view");
        createMenuIfNotExists(4L, 0L, "系统管理", "/system", null, "Setting", 99, "M", null);

        createMenuIfNotExists(5L, 4L, "角色管理", "/system/role", "role/index", "User", 1, "C", "system:role:view");
        createMenuIfNotExists(6L, 4L, "菜单管理", "/system/menu", "menu/index", "Menu", 2, "C", "system:menu:view");
        createMenuIfNotExists(7L, 4L, "用户管理", "/system/user", "user/index", "UserFilled", 3, "C", "system:user:view");
        createMenuIfNotExists(8L, 4L, "字典管理", "/system/dict", "sys-dict/index", "Reading", 4, "C", "system:dict:view");
        createMenuIfNotExists(9L, 4L, "参数配置", "/system/config", "sys-config/index", "Operation", 5, "C", "system:config:view");
        createMenuIfNotExists(10L, 4L, "日志管理", "/system/log", "log/index", "Tickets", 6, "C", "system:log:view");

        createMenuIfNotExists(101L, 3L, "工单新增", null, null, null, 1, "F", "workorder:add");
        createMenuIfNotExists(102L, 3L, "工单编辑", null, null, null, 2, "F", "workorder:edit");
        createMenuIfNotExists(103L, 3L, "工单删除", null, null, null, 3, "F", "workorder:delete");
        createMenuIfNotExists(104L, 3L, "工单派单", null, null, null, 4, "F", "workorder:dispatch");
        createMenuIfNotExists(105L, 3L, "工单接单", null, null, null, 5, "F", "workorder:accept");
        createMenuIfNotExists(106L, 3L, "工单完成", null, null, null, 6, "F", "workorder:complete");
        createMenuIfNotExists(107L, 3L, "工单导出", null, null, null, 7, "F", "workorder:export");

        createMenuIfNotExists(201L, 2L, "资产新增", null, null, null, 1, "F", "asset:add");
        createMenuIfNotExists(202L, 2L, "资产编辑", null, null, null, 2, "F", "asset:edit");
        createMenuIfNotExists(203L, 2L, "资产删除", null, null, null, 3, "F", "asset:delete");
        createMenuIfNotExists(204L, 2L, "资产报废", null, null, null, 4, "F", "asset:scrap");
        createMenuIfNotExists(205L, 2L, "批量报废", null, null, null, 5, "F", "asset:batchScrap");
        createMenuIfNotExists(206L, 2L, "资产导出", null, null, null, 6, "F", "asset:export");

        log.info("========== 预设菜单初始化完成 ==========");
    }

    private void createMenuIfNotExists(Long id, Long parentId, String menuName, String path,
                                       String component, String icon, Integer sortOrder,
                                       String menuType, String perms) {
        MenuEntity existMenu = menuMapper.selectById(id);
        if (existMenu == null) {
            MenuEntity menu = new MenuEntity();
            menu.setId(id);
            menu.setParentId(parentId);
            menu.setMenuName(menuName);
            menu.setPath(path);
            menu.setComponent(component);
            menu.setIcon(icon);
            menu.setSortOrder(sortOrder);
            menu.setMenuType(menuType);
            menu.setPerms(perms);
            menu.setIsExternal(0);
            menu.setIsHidden(0);
            menu.setStatus(1);
            menu.setVisible("1");
            menu.setKeepAlive("1");
            menu.setAlwaysShow("1");
            menu.setIsDeleted(0);
            menuMapper.insert(menu);
            log.info("Created menu: {} - {}", id, menuName);
        }
    }

    private void initializeRoleMenus() {
        log.info("========== 开始初始化角色菜单权限 ==========");

        RoleEntity superAdmin = roleMapper.selectOne(new LambdaQueryWrapper<RoleEntity>()
                .eq(RoleEntity::getRoleCode, "SUPER_ADMIN"));
        RoleEntity admin = roleMapper.selectOne(new LambdaQueryWrapper<RoleEntity>()
                .eq(RoleEntity::getRoleCode, "ADMIN"));
        RoleEntity engineer = roleMapper.selectOne(new LambdaQueryWrapper<RoleEntity>()
                .eq(RoleEntity::getRoleCode, "ENGINEER"));
        RoleEntity user = roleMapper.selectOne(new LambdaQueryWrapper<RoleEntity>()
                .eq(RoleEntity::getRoleCode, "USER"));

        if (superAdmin != null) {
            bindAllMenusToRole(superAdmin.getId());
        }

        if (admin != null) {
            bindMenusToRole(admin.getId(), new Long[]{1L, 2L, 3L, 7L, 8L, 9L,
                    101L, 102L, 103L, 107L,
                    201L, 202L, 203L, 206L});
        }

        if (engineer != null) {
            bindMenusToRole(engineer.getId(), new Long[]{1L, 3L, 105L, 106L});
        }

        if (user != null) {
            bindMenusToRole(user.getId(), new Long[]{1L, 3L, 101L});
        }

        log.info("========== 角色菜单权限初始化完成 ==========");
    }

    private void bindAllMenusToRole(Long roleId) {
        List<MenuEntity> allMenus = menuMapper.selectList(null);
        for (MenuEntity menu : allMenus) {
            RoleMenuEntity exist = roleMenuMapper.selectOne(new LambdaQueryWrapper<RoleMenuEntity>()
                    .eq(RoleMenuEntity::getRoleId, roleId)
                    .eq(RoleMenuEntity::getMenuId, menu.getId()));
            if (exist == null) {
                RoleMenuEntity rm = new RoleMenuEntity();
                rm.setRoleId(roleId);
                rm.setMenuId(menu.getId());
                roleMenuMapper.insert(rm);
            }
        }
    }

    private void bindMenusToRole(Long roleId, Long[] menuIds) {
        for (Long menuId : menuIds) {
            RoleMenuEntity exist = roleMenuMapper.selectOne(new LambdaQueryWrapper<RoleMenuEntity>()
                    .eq(RoleMenuEntity::getRoleId, roleId)
                    .eq(RoleMenuEntity::getMenuId, menuId));
            if (exist == null) {
                RoleMenuEntity rm = new RoleMenuEntity();
                rm.setRoleId(roleId);
                rm.setMenuId(menuId);
                roleMenuMapper.insert(rm);
            }
        }
    }
}
