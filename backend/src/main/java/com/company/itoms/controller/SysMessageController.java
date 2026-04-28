package com.company.itoms.controller;

import com.company.itoms.entity.SysMessageEntity;
import com.company.itoms.service.ISysMessageService;
import com.company.itoms.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sys-message")
public class SysMessageController {

    @Autowired
    private ISysMessageService sysMessageService;

    @GetMapping("/list")
    public Result<List<SysMessageEntity>> list() {
        return Result.success(sysMessageService.list());
    }

    @GetMapping("/{id}")
    public Result<SysMessageEntity> getById(@PathVariable Long id) {
        return Result.success(sysMessageService.getById(id));
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody SysMessageEntity entity) {
        return Result.success(sysMessageService.save(entity));
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody SysMessageEntity entity) {
        return Result.success(sysMessageService.updateById(entity));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(sysMessageService.removeById(id));
    }

    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount() {
        return Result.success(0L);
    }
}
