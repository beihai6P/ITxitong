package com.company.itoms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.company.itoms.dto.AiRecommendationDTO;
import com.company.itoms.dto.request.WorkOrderCreateDTO;
import com.company.itoms.entity.WorkOrderEntity;
import com.company.itoms.entity.WorkOrderDispatchLogEntity;
import com.company.itoms.entity.EngineerWorkStatEntity;

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

import java.util.List;
import java.util.Map;

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
    
    List<WorkOrderEntity> getWorkOrderList(Integer status, Integer type, Integer priority, Long engineerId, Integer isTimeout, Boolean isMyOrder, Long currentUserId);
    
    Map<String, Object> getWorkOrderStats();
    
    List<Map<String, Object>> getWorkOrderTrend();
    
    List<Map<String, Object>> getTypeDistribution();
    
    List<Map<String, Object>> getWorkOrderLogs(Long id);
    
    List<Map<String, Object>> getAssignees();
    
    // 自动派单引擎
    boolean autoDispatchWorkOrders();
    
    // 手动派单
    boolean manualDispatch(Long workOrderId, Long engineerId, Long operatorId, String operatorName);
    
    // 工程师接单
    boolean acceptWorkOrder(Long workOrderId, Long engineerId, Long operatorId, String operatorName);
    
    // 工程师拒单
    boolean refuseWorkOrder(Long workOrderId, Long engineerId, String reason, Long operatorId, String operatorName);
    
    // 转派工单
    boolean transferWorkOrder(Long workOrderId, Long newEngineerId, Long operatorId, String operatorName);
    
    // 更新处理进度
    boolean updateProcessProgress(Long workOrderId, Integer progress, Long operatorId, String operatorName);
    
    // 获取派单记录
    List<WorkOrderDispatchLogEntity> getDispatchLogs(Long workOrderId);
    
    // 获取工程师工作量统计
    List<EngineerWorkStatEntity> getEngineerWorkStats(java.time.LocalDate startDate, java.time.LocalDate endDate);
    
    // 生成工程师工作量统计
    boolean generateEngineerWorkStats(java.time.LocalDate date);
    
    // 获取工程师工作量占比
    List<Map<String, Object>> getEngineerWorkloadDistribution();
    
    // 获取工单状态漏斗
    List<Map<String, Object>> getWorkOrderStatusFunnel();
    
    // 获取工程师统计
    List<Map<String, Object>> getEngineerStats(java.time.LocalDate startDate, java.time.LocalDate endDate);
}
