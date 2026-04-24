package com.company.itoms.controller;

import com.company.itoms.common.Result;
import com.company.itoms.entity.MenuEntity;
import com.company.itoms.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "菜单管理")
@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @Operation(summary = "获取所有菜单")
    @GetMapping("/list")
    public Result<List<MenuEntity>> list() {
        return Result.success(menuService.list());
    }

    @Operation(summary = "根据ID获取菜单")
    @GetMapping("/{id}")
    public Result<MenuEntity> getById(@PathVariable Long id) {
        return Result.success(menuService.getById(id));
    }

    @Operation(summary = "新增菜单")
    @PostMapping
    public Result<Boolean> save(@RequestBody MenuEntity menu) {
        return Result.success(menuService.save(menu));
    }

    @Operation(summary = "修改菜单")
    @PutMapping
    public Result<Boolean> update(@RequestBody MenuEntity menu) {
        return Result.success(menuService.updateById(menu));
    }

    @Operation(summary = "删除菜单")
    @DeleteMapping("/{id}")
    public Result<Boolean> remove(@PathVariable Long id) {
        return Result.success(menuService.removeById(id));
    }
}
