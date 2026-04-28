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

    public List<WorkOrderEntity> getWorkOrders() {
        return this.workOrders;
    }

    public void setWorkOrders(List<WorkOrderEntity> workOrders) {
        this.workOrders = workOrders;
    }

    public List<AssetEntity> getAssets() {
        return this.assets;
    }

    public void setAssets(List<AssetEntity> assets) {
        this.assets = assets;
    }

    public List<KnowledgeBaseEntity> getKnowledgeBases() {
        return this.knowledgeBases;
    }

    public void setKnowledgeBases(List<KnowledgeBaseEntity> knowledgeBases) {
        this.knowledgeBases = knowledgeBases;
    }

}
