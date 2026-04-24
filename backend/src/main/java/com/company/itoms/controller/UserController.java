package com.company.itoms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.itoms.common.Result;
import com.company.itoms.entity.UserEntity;
import com.company.itoms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

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
        // Wipe out passwords before returning
        page.getRecords().forEach(u -> u.setPassword(null));
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<UserEntity> getById(@PathVariable Long id) {
        UserEntity user = userService.getById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    @PostMapping
    public Result<Boolean> create(@RequestBody UserEntity user) {
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            // Default password if not provided
            user.setPassword(passwordEncoder.encode("123456"));
        }
        return Result.success(userService.save(user));
    }

    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody UserEntity user) {
        user.setId(id);
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null); // do not update password if not provided
        }
        return Result.success(userService.updateById(user));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(userService.removeById(id));
    }
}
