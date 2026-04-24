package com.company.itoms.controller;

import com.company.itoms.common.Result;
import com.company.itoms.entity.OperationLogEntity;
import com.company.itoms.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "操作日志")
@RestController
@RequestMapping("/log")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogService operationLogService;

    @Operation(summary = "获取所有日志")
    @GetMapping("/list")
    public Result<List<OperationLogEntity>> list() {
        return Result.success(operationLogService.list());
    }

    @Operation(summary = "根据ID获取日志")
    @GetMapping("/{id}")
    public Result<OperationLogEntity> getById(@PathVariable Long id) {
        return Result.success(operationLogService.getById(id));
    }

    @Operation(summary = "新增日志")
    @PostMapping
    public Result<Boolean> save(@RequestBody OperationLogEntity log) {
        return Result.success(operationLogService.save(log));
    }

    @Operation(summary = "删除日志")
    @DeleteMapping("/{id}")
    public Result<Boolean> remove(@PathVariable Long id) {
        return Result.success(operationLogService.removeById(id));
    }
}
