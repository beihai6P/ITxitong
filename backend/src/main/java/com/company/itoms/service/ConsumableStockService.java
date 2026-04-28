package com.company.itoms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.company.itoms.entity.ConsumableStockEntity;
import com.company.itoms.dto.request.ConsumableUsageDTO;
import java.util.List;

public interface ConsumableStockService extends IService<ConsumableStockEntity> {
    void deductStock(List<ConsumableUsageDTO> usages, Long workOrderId);
}
