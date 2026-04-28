package com.company.itoms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.itoms.dto.request.ConsumableUsageDTO;
import com.company.itoms.entity.ConsumableStockEntity;
import com.company.itoms.mapper.ConsumableStockMapper;
import com.company.itoms.service.ConsumableStockService;
import lombok.extern.slf.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConsumableStockServiceImpl extends ServiceImpl<ConsumableStockMapper, ConsumableStockEntity> implements ConsumableStockService {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ConsumableStockServiceImpl.class);

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductStock(List<ConsumableUsageDTO> usages, Long workOrderId) {
        if (usages == null || usages.isEmpty()) {
            return;
        }

        for (ConsumableUsageDTO usage : usages) {
            ConsumableStockEntity stock = this.getById(usage.getConsumableId());
            if (stock == null) {
                throw new IllegalStateException("耗材不存在: " + usage.getConsumableId());
            }

            if (stock.getStockQuantity() < usage.getQuantity()) {
                throw new IllegalStateException("耗材库存不足: " + stock.getName() + " (剩余: " + stock.getStockQuantity() + ")");
            }

            stock.setStockQuantity(stock.getStockQuantity() - usage.getQuantity());
            this.updateById(stock);

            // 检查安全阈值预警
            if (stock.getStockQuantity() < stock.getSafeThreshold()) {
                log.warn("【系统预警】耗材 [{}] 库存低于安全阈值，当前剩余: {}", stock.getName(), stock.getStockQuantity());
                // TODO: 可以发布事件生成采购工单或发通知
            }

            // 记录耗材使用日志（假设存在 consumable_log 表）
            log.info("工单 [{}] 消耗耗材 [{}], 数量: {}", workOrderId, stock.getName(), usage.getQuantity());
        }
    }
}
