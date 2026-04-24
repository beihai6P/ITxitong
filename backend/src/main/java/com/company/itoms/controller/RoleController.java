package com.company.itoms.controller;

import com.company.itoms.common.Result;
import com.company.itoms.entity.RoleEntity;
import com.company.itoms.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "角色管理")
@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "获取所有角色")
    @GetMapping("/list")
    public Result<List<RoleEntity>> list() {
        return Result.success(roleService.list());
    }

    @Operation(summary = "根据ID获取角色")
    @GetMapping("/{id}")
    public Result<RoleEntity> getById(@PathVariable Long id) {
        return Result.success(roleService.getById(id));
    }

    @Operation(summary = "新增角色")
    @PostMapping
    public Result<Boolean> save(@RequestBody RoleEntity role) {
        return Result.success(roleService.save(role));
    }

    @Operation(summary = "修改角色")
    @PutMapping
    public Result<Boolean> update(@RequestBody RoleEntity role) {
        return Result.success(roleService.updateById(role));
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    public Result<Boolean> remove(@PathVariable Long id) {
        return Result.success(roleService.removeById(id));
    }
}
