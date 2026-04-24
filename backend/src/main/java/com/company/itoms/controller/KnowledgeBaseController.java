package com.company.itoms.controller;

import com.company.itoms.common.Result;
import com.company.itoms.entity.KnowledgeBaseEntity;
import com.company.itoms.service.KnowledgeBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/knowledge-base")
public class KnowledgeBaseController {

    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    @GetMapping("/{id}")
    public Result<KnowledgeBaseEntity> getKnowledgeBase(@PathVariable Long id) {
        return Result.success(knowledgeBaseService.getById(id));
    }
}
