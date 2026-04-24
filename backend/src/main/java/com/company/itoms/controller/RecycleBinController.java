package com.company.itoms.controller;

import com.company.itoms.common.Result;
import com.company.itoms.entity.AssetEntity;
import com.company.itoms.entity.KnowledgeBaseEntity;
import com.company.itoms.entity.WorkOrderEntity;
import com.company.itoms.mapper.AssetMapper;
import com.company.itoms.mapper.KnowledgeBaseMapper;
import com.company.itoms.mapper.WorkOrderMapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recycle-bin")
public class RecycleBinController {

    @Autowired
    private WorkOrderMapper workOrderMapper;

    @Autowired
    private AssetMapper assetMapper;

    @Autowired
    private KnowledgeBaseMapper knowledgeBaseMapper;

    // --- WorkOrder ---
    @GetMapping("/work-orders")
    public Result<List<WorkOrderEntity>> listDeletedWorkOrders() {
        // Need to add this to mapper or execute raw sql
        return Result.success(workOrderMapper.selectDeleted());
    }

    @PostMapping("/work-orders/{id}/restore")
    public Result<Boolean> restoreWorkOrder(@PathVariable Long id) {
        return Result.success(workOrderMapper.restoreById(id) > 0);
    }

    // --- Asset ---
    @GetMapping("/assets")
    public Result<List<AssetEntity>> listDeletedAssets() {
        return Result.success(assetMapper.selectDeleted());
    }

    @PostMapping("/assets/{id}/restore")
    public Result<Boolean> restoreAsset(@PathVariable Long id) {
        return Result.success(assetMapper.restoreById(id) > 0);
    }

    // --- KnowledgeBase ---
    @GetMapping("/knowledge-bases")
    public Result<List<KnowledgeBaseEntity>> listDeletedKnowledgeBases() {
        return Result.success(knowledgeBaseMapper.selectDeleted());
    }

    @PostMapping("/knowledge-bases/{id}/restore")
    public Result<Boolean> restoreKnowledgeBase(@PathVariable Long id) {
        return Result.success(knowledgeBaseMapper.restoreById(id) > 0);
    }
}
