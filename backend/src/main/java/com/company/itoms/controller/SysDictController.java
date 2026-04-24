package com.company.itoms.controller;

import com.company.itoms.entity.SysDictEntity;
import com.company.itoms.service.ISysDictService;
import com.company.itoms.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sys-dict")
public class SysDictController {

    @Autowired
    private ISysDictService sysDictService;

    @GetMapping("/list")
    public Result<List<SysDictEntity>> list() {
        return Result.success(sysDictService.list());
    }

    @GetMapping("/{id}")
    public Result<SysDictEntity> getById(@PathVariable Long id) {
        return Result.success(sysDictService.getById(id));
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody SysDictEntity entity) {
        return Result.success(sysDictService.save(entity));
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody SysDictEntity entity) {
        return Result.success(sysDictService.updateById(entity));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(sysDictService.removeById(id));
    }
}
