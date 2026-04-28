package com.company.itoms.controller;

import com.company.itoms.common.Result;
import com.company.itoms.config.DataInitRunner;
import com.company.itoms.entity.MenuEntity;
import com.company.itoms.entity.RoleEntity;
import com.company.itoms.service.MenuService;
import com.company.itoms.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "系统初始化")
@RestController
@RequestMapping("/api/v1/init")
@RequiredArgsConstructor
public class InitController {

    private final MenuService menuService;
    private final RoleService roleService;
    private final DataInitRunner dataInitRunner;

    @Operation(summary = "检查系统初始化状态")
    @GetMapping("/status")
    public Result<String> checkStatus() {
        List<MenuEntity> menus = menuService.list();
        List<RoleEntity> roles = roleService.list();

        StringBuilder status = new StringBuilder();
        status.append("菜单数量: ").append(menus.size()).append("<br>");
        status.append("角色数量: ").append(roles.size()).append("<br>");

        if (menus.size() < 10) {
            status.append("<span style='color:red'>⚠️ 菜单数据不足，需要执行数据库初始化脚本</span>");
        } else {
            status.append("<span style='color:green'>✓ 菜单数据正常</span>");
        }

        if (roles.size() < 4) {
            status.append("<br><span style='color:red'>⚠️ 角色数据不足，需要执行数据库初始化脚本</span>");
        } else {
            status.append("<br><span style='color:green'>✓ 角色数据正常</span>");
        }

        return Result.success(status.toString());
    }

    @Operation(summary = "手动触发数据初始化(慎用)")
    @PostMapping("/reload")
    public Result<String> reload() {
        try {
            dataInitRunner.run();
            return Result.success("数据初始化完成，请重新登录");
        } catch (Exception e) {
            return Result.error(500, "初始化失败: " + e.getMessage());
        }
    }
}
