package com.company.itoms.dto.response;

import com.company.itoms.entity.AssetEntity;
import com.company.itoms.entity.KnowledgeBaseEntity;
import com.company.itoms.entity.WorkOrderEntity;
import lombok.Data;

import java.util.List;

@Data
public class GlobalSearchResult {
    private List<WorkOrderEntity> workOrders;
    private List<AssetEntity> assets;
    private List<KnowledgeBaseEntity> knowledgeBases;
}
