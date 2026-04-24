package com.company.itoms.controller;

import com.company.itoms.entity.SysConfigEntity;
import com.company.itoms.service.ISysConfigService;
import com.company.itoms.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sys-config")
public class SysConfigController {

    @Autowired
    private ISysConfigService sysConfigService;

    @GetMapping("/list")
    public Result<List<SysConfigEntity>> list() {
        return Result.success(sysConfigService.list());
    }

    @GetMapping("/{id}")
    public Result<SysConfigEntity> getById(@PathVariable Long id) {
        return Result.success(sysConfigService.getById(id));
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody SysConfigEntity entity) {
        return Result.success(sysConfigService.save(entity));
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody SysConfigEntity entity) {
        return Result.success(sysConfigService.updateById(entity));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(sysConfigService.removeById(id));
    }
}
