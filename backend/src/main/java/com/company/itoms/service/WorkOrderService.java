package com.company.itoms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.company.itoms.dto.AiRecommendationDTO;
import com.company.itoms.dto.request.WorkOrderCreateDTO;
import com.company.itoms.entity.WorkOrderEntity;

import com.company.itoms.dto.request.WorkOrderAssignDTO;
import com.company.itoms.dto.request.WorkOrderProcessDTO;
import com.company.itoms.dto.request.WorkOrderFinishDTO;
import com.company.itoms.dto.request.WorkOrderEvaluateDTO;
import com.company.itoms.dto.request.WorkOrderGrabDTO;
import com.company.itoms.dto.request.WorkOrderRejectDTO;
import com.company.itoms.dto.request.WorkOrderTransferDTO;
import com.company.itoms.dto.request.WorkOrderSuspendDTO;
import com.company.itoms.dto.request.WorkOrderBatchAssignDTO;
import com.company.itoms.dto.request.WorkOrderBatchCloseDTO;

public interface WorkOrderService extends IService<WorkOrderEntity> {
    AiRecommendationDTO getRecommendation(WorkOrderCreateDTO dto);
    
    Long createWorkOrder(WorkOrderCreateDTO dto);

    void assignWorkOrder(WorkOrderAssignDTO dto);
    
    void autoAssignWorkOrder(Long workOrderId);

    void batchAssign(WorkOrderBatchAssignDTO dto);
    
    void batchClose(WorkOrderBatchCloseDTO dto);

    
    void grabOrder(WorkOrderGrabDTO dto);
    
    void rejectOrder(WorkOrderRejectDTO dto);
    
    void transferOrder(WorkOrderTransferDTO dto);
    
    void suspendOrder(WorkOrderSuspendDTO dto);

    void processWorkOrder(WorkOrderProcessDTO dto);

    void finishWorkOrder(WorkOrderFinishDTO dto);

    void evaluateWorkOrder(WorkOrderEvaluateDTO dto);
}
