package com.company.itoms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.itoms.common.Result;
import com.company.itoms.entity.RoleEntity;
import com.company.itoms.entity.UserEntity;
import com.company.itoms.entity.UserRoleEntity;
import com.company.itoms.mapper.UserRoleMapper;
import com.company.itoms.service.RoleService;
import com.company.itoms.service.UserRoleService;
import com.company.itoms.service.UserService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;
    private final UserRoleService userRoleService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final UserRoleMapper userRoleMapper;

    @GetMapping
    public Result<Page<UserEntity>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName) {

        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(username)) {
            wrapper.like(UserEntity::getUsername, username);
        }
        if (StringUtils.hasText(realName)) {
            wrapper.like(UserEntity::getRealName, realName);
        }

        Page<UserEntity> page = userService.page(new Page<>(current, size), wrapper);
        page.getRecords().forEach(u -> u.setPassword(null));

        for (UserEntity user : page.getRecords()) {
            List<Long> roleIds = userRoleService.getRoleIdsByUserId(user.getId());
            user.setRoleIds(roleIds);

            if (roleIds != null && !roleIds.isEmpty()) {
                List<RoleEntity> roles = roleService.listByIds(roleIds);
                user.setRoleNames(roles.stream().map(RoleEntity::getRoleName).collect(Collectors.toList()));
            } else {
                user.setRoleNames(new ArrayList<>());
            }
        }

        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<UserEntity> getById(@PathVariable @NotNull(message = "用户ID不能为空") Long id) {
        UserEntity user = userService.getById(id);
        if (user != null) {
            user.setPassword(null);
            List<Long> roleIds = userRoleService.getRoleIdsByUserId(id);
            user.setRoleIds(roleIds);
        }
        return Result.success(user);
    }

    @PostMapping
    public Result<Boolean> create(@Validated @RequestBody UserEntity user) {
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(passwordEncoder.encode("123456"));
        }
        user.setIsDeleted(0);

        String roleValue = user.getRole();
        if (!StringUtils.hasText(roleValue)) {
            user.setRole("USER");
        }

        boolean saved = userService.save(user);

        if (saved && user.getRoleIds() != null && !user.getRoleIds().isEmpty()) {
            userRoleService.assignRoles(user.getId(), user.getRoleIds());
        } else if (saved && StringUtils.hasText(user.getRole())) {
            Long roleId = getRoleIdByCode(user.getRole());
            if (roleId != null) {
                userRoleService.assignRoles(user.getId(), java.util.Collections.singletonList(roleId));
            }
        }

        return Result.success(saved);
    }

    private Long getRoleIdByCode(String roleCode) {
        List<RoleEntity> roles = roleService.list();
        return roles.stream()
                .filter(r -> r.getRoleCode().equalsIgnoreCase(roleCode))
                .findFirst()
                .map(RoleEntity::getId)
                .orElse(null);
    }

    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable @NotNull(message = "用户ID不能为空") Long id, @Validated @RequestBody UserEntity user) {
        user.setId(id);
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null);
        }
        boolean updated = userService.updateById(user);

        if (updated) {
            userRoleService.removeAllRoles(id);
            if (user.getRoleIds() != null && !user.getRoleIds().isEmpty()) {
                userRoleService.assignRoles(id, user.getRoleIds());
            }
        }

        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable @NotNull(message = "用户ID不能为空") Long id) {
        userRoleService.removeAllRoles(id);
        return Result.success(userService.removeById(id));
    }

    @GetMapping("/{id}/roles")
    public Result<List<RoleEntity>> getUserRoles(@PathVariable @NotNull(message = "用户ID不能为空") Long id) {
        List<Long> roleIds = userRoleService.getRoleIdsByUserId(id);
        if (roleIds == null || roleIds.isEmpty()) {
            return Result.success(new ArrayList<>());
        }
        List<RoleEntity> roles = roleService.listByIds(roleIds);
        return Result.success(roles);
    }

    @PutMapping("/{id}/roles")
    public Result<Boolean> assignRoles(@PathVariable @NotNull(message = "用户ID不能为空") Long id, @RequestBody List<Long> roleIds) {
        userRoleService.removeAllRoles(id);
        if (roleIds != null && !roleIds.isEmpty()) {
            userRoleService.assignRoles(id, roleIds);
        }
        return Result.success(true);
    }
}
