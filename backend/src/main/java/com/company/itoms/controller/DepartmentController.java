package com.company.itoms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.itoms.common.Result;
import com.company.itoms.entity.DepartmentEntity;
import com.company.itoms.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/depts")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    public Result<List<DepartmentEntity>> list(@RequestParam(required = false) String name) {
        LambdaQueryWrapper<DepartmentEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(DepartmentEntity::getName, name);
        }
        wrapper.orderByAsc(DepartmentEntity::getSortOrder);
        return Result.success(departmentService.list(wrapper));
    }

    @GetMapping("/{id}")
    public Result<DepartmentEntity> getById(@PathVariable Long id) {
        return Result.success(departmentService.getById(id));
    }

    @PostMapping
    public Result<Boolean> create(@RequestBody DepartmentEntity department) {
        return Result.success(departmentService.save(department));
    }

    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody DepartmentEntity department) {
        department.setId(id);
        return Result.success(departmentService.updateById(department));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(departmentService.removeById(id));
    }
}
