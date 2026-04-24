package com.company.itoms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.company.itoms.common.Result;
import com.company.itoms.dto.response.GlobalSearchResult;
import com.company.itoms.entity.AssetEntity;
import com.company.itoms.entity.KnowledgeBaseEntity;
import com.company.itoms.entity.WorkOrderEntity;
import com.company.itoms.service.AssetService;
import com.company.itoms.service.KnowledgeBaseService;
import com.company.itoms.service.WorkOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class GlobalSearchController {

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private AssetService assetService;

    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    @GetMapping
    public Result<GlobalSearchResult> search(@RequestParam String keyword) {
        GlobalSearchResult result = new GlobalSearchResult();

        // 搜索工单
        QueryWrapper<WorkOrderEntity> woQuery = new QueryWrapper<>();
        woQuery.like("work_order_code", keyword).or().like("description", keyword);
        woQuery.last("LIMIT 10");
        List<WorkOrderEntity> workOrders = workOrderService.list(woQuery);
        result.setWorkOrders(workOrders);

        // 搜索资产
        QueryWrapper<AssetEntity> assetQuery = new QueryWrapper<>();
        assetQuery.like("asset_code", keyword).or().like("asset_name", keyword);
        assetQuery.last("LIMIT 10");
        List<AssetEntity> assets = assetService.list(assetQuery);
        result.setAssets(assets);

        // 搜索知识库
        QueryWrapper<KnowledgeBaseEntity> kbQuery = new QueryWrapper<>();
        kbQuery.like("symptom", keyword).or().like("solution", keyword);
        kbQuery.last("LIMIT 10");
        List<KnowledgeBaseEntity> knowledgeBases = knowledgeBaseService.list(kbQuery);
        result.setKnowledgeBases(knowledgeBases);

        return Result.success(result);
    }
}
