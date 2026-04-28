package com.company.itoms.controller;

import com.company.itoms.common.Idempotent;
import com.company.itoms.common.Result;
import com.company.itoms.dto.AiRecommendationDTO;
import com.company.itoms.dto.request.WorkOrderCreateDTO;
import com.company.itoms.dto.request.WorkOrderAssignDTO;
import com.company.itoms.dto.request.WorkOrderProcessDTO;
import com.company.itoms.dto.request.WorkOrderFinishDTO;
import com.company.itoms.dto.request.WorkOrderEvaluateDTO;
import com.company.itoms.dto.request.WorkOrderGrabDTO;
import com.company.itoms.dto.request.WorkOrderRejectDTO;
import com.company.itoms.dto.request.WorkOrderTransferDTO;
import com.company.itoms.dto.request.WorkOrderSuspendDTO;
import com.company.itoms.entity.WorkOrderEntity;
import com.company.itoms.entity.WorkOrderDispatchLogEntity;
import com.company.itoms.entity.EngineerWorkStatEntity;
import com.company.itoms.exception.BusinessException;
import com.company.itoms.service.WorkOrderService;
import com.company.itoms.util.HtmlUtils;
import com.company.itoms.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/work-order")
public class WorkOrderController {

    @Autowired
    private WorkOrderService workOrderService;

    @PostMapping("/create")
    @Idempotent(key = "#dto.workOrderCode")
    public Result<Long> createWorkOrder(@Validated @RequestBody WorkOrderCreateDTO dto) {
        Long id = workOrderService.createWorkOrder(dto);
        return Result.success(id);
    }

    @PostMapping("/recommendation")
    public Result<AiRecommendationDTO> getRecommendation(@Validated @RequestBody WorkOrderCreateDTO dto) {
        AiRecommendationDTO recommendation = workOrderService.getRecommendation(dto);
        return Result.success(recommendation);
    }

    @GetMapping("/{id}")
    public Result<WorkOrderEntity> getWorkOrder(@PathVariable Long id) {
        WorkOrderEntity entity = workOrderService.getById(id);
        if (entity != null) {
            // XSS防护示例
            entity.setDescription(HtmlUtils.htmlEscape(entity.getDescription()));
        }
        return Result.success(entity);
    }

    @PostMapping("/assign")
    public Result<Void> assignWorkOrder(@Validated @RequestBody WorkOrderAssignDTO dto) {
        workOrderService.assignWorkOrder(dto);
        return Result.success(null);
    }

    @PostMapping("/auto-assign/{id}")
    public Result<Void> autoAssignWorkOrder(@PathVariable Long id) {
        workOrderService.autoAssignWorkOrder(id);
        return Result.success(null);
    }

    @PostMapping("/grab")
    public Result<Void> grabOrder(@Validated @RequestBody WorkOrderGrabDTO dto) {
        workOrderService.grabOrder(dto);
        return Result.success(null);
    }

    @PostMapping("/reject")
    public Result<Void> rejectOrder(@Validated @RequestBody WorkOrderRejectDTO dto) {
        workOrderService.rejectOrder(dto);
        return Result.success(null);
    }

    @PostMapping("/transfer")
    public Result<Void> transferOrder(@Validated @RequestBody WorkOrderTransferDTO dto) {
        workOrderService.transferOrder(dto);
        return Result.success(null);
    }

    @PostMapping("/suspend")
    public Result<Void> suspendOrder(@Validated @RequestBody WorkOrderSuspendDTO dto) {
        workOrderService.suspendOrder(dto);
        return Result.success(null);
    }

    @PostMapping("/process")
    public Result<Void> processWorkOrder(@Validated @RequestBody WorkOrderProcessDTO dto) {
        workOrderService.processWorkOrder(dto);
        return Result.success(null);
    }

    @PostMapping("/finish")
    public Result<Void> finishWorkOrder(@Validated @RequestBody WorkOrderFinishDTO dto) {
        workOrderService.finishWorkOrder(dto);
        return Result.success(null);
    }

    @PostMapping("/evaluate")
    public Result<Void> evaluateWorkOrder(@Validated @RequestBody WorkOrderEvaluateDTO dto) {
        workOrderService.evaluateWorkOrder(dto);
        return Result.success(null);
    }

    @GetMapping("/list")
    public Result<List<WorkOrderEntity>> getWorkOrderList(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer priority,
            @RequestParam(required = false) Long engineerId,
            @RequestParam(required = false) Integer isTimeout,
            @RequestParam(required = false) Boolean isMyOrder) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(401, "未登录或登录已过期");
        }
        List<WorkOrderEntity> list = workOrderService.getWorkOrderList(status, type, priority, engineerId, isTimeout, isMyOrder, currentUserId);
        return Result.success(list);
    }

    @GetMapping("/overview")
    public Result<Map<String, Object>> getWorkOrderOverview() {
        Map<String, Object> stats = workOrderService.getWorkOrderStats();
        return Result.success(stats);
    }

    @GetMapping("/chart/7days")
    public Result<List<Map<String, Object>>> getWorkOrderTrend() {
        List<Map<String, Object>> trend = workOrderService.getWorkOrderTrend();
        return Result.success(trend);
    }

    @GetMapping("/chart/type")
    public Result<List<Map<String, Object>>> getTypeDistribution() {
        List<Map<String, Object>> distribution = workOrderService.getTypeDistribution();
        return Result.success(distribution);
    }

    @GetMapping("/chart/engineer")
    public Result<List<Map<String, Object>>> getEngineerWorkloadDistribution() {
        List<Map<String, Object>> distribution = workOrderService.getEngineerWorkloadDistribution();
        return Result.success(distribution);
    }

    @GetMapping("/chart/funnel")
    public Result<List<Map<String, Object>>> getWorkOrderStatusFunnel() {
        List<Map<String, Object>> funnel = workOrderService.getWorkOrderStatusFunnel();
        return Result.success(funnel);
    }

    @GetMapping("/engineer-stats")
    public Result<List<Map<String, Object>>> getEngineerStats(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now();
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
        List<Map<String, Object>> stats = workOrderService.getEngineerStats(start, end);
        return Result.success(stats);
    }

    @PostMapping("/dispatch/auto")
    public Result<Boolean> autoDispatchWorkOrders() {
        boolean success = workOrderService.autoDispatchWorkOrders();
        return Result.success(success);
    }

    @PostMapping("/dispatch/manual")
    public Result<Boolean> manualDispatch(
            @RequestParam Long workOrderId,
            @RequestParam Long engineerId,
            @RequestParam Long operatorId,
            @RequestParam String operatorName) {
        boolean success = workOrderService.manualDispatch(workOrderId, engineerId, operatorId, operatorName);
        return Result.success(success);
    }

    @PostMapping("/dispatch/accept")
    public Result<Boolean> acceptWorkOrder(
            @RequestParam Long workOrderId,
            @RequestParam Long engineerId,
            @RequestParam Long operatorId,
            @RequestParam String operatorName) {
        boolean success = workOrderService.acceptWorkOrder(workOrderId, engineerId, operatorId, operatorName);
        return Result.success(success);
    }

    @PostMapping("/dispatch/refuse")
    public Result<Boolean> refuseWorkOrder(
            @RequestParam Long workOrderId,
            @RequestParam Long engineerId,
            @RequestParam String reason,
            @RequestParam Long operatorId,
            @RequestParam String operatorName) {
        boolean success = workOrderService.refuseWorkOrder(workOrderId, engineerId, reason, operatorId, operatorName);
        return Result.success(success);
    }

    @PostMapping("/dispatch/transfer")
    public Result<Boolean> transferWorkOrder(
            @RequestParam Long workOrderId,
            @RequestParam Long newEngineerId,
            @RequestParam Long operatorId,
            @RequestParam String operatorName) {
        boolean success = workOrderService.transferWorkOrder(workOrderId, newEngineerId, operatorId, operatorName);
        return Result.success(success);
    }

    @PostMapping("/update-progress")
    public Result<Boolean> updateProcessProgress(
            @RequestParam Long workOrderId,
            @RequestParam Integer progress,
            @RequestParam Long operatorId,
            @RequestParam String operatorName) {
        boolean success = workOrderService.updateProcessProgress(workOrderId, progress, operatorId, operatorName);
        return Result.success(success);
    }

    @GetMapping("/flow-log/{orderId}")
    public Result<List<Map<String, Object>>> getWorkOrderLogs(@PathVariable Long orderId) {
        List<Map<String, Object>> logs = workOrderService.getWorkOrderLogs(orderId);
        return Result.success(logs);
    }

    @GetMapping("/dispatch-log/{orderId}")
    public Result<List<WorkOrderDispatchLogEntity>> getDispatchLogs(@PathVariable Long orderId) {
        List<WorkOrderDispatchLogEntity> logs = workOrderService.getDispatchLogs(orderId);
        return Result.success(logs);
    }



    @PostMapping("/engineer-stats/generate")
    public Result<Boolean> generateEngineerWorkStats(@RequestParam String date) {
        LocalDate statDate = LocalDate.parse(date);
        boolean success = workOrderService.generateEngineerWorkStats(statDate);
        return Result.success(success);
    }

    @GetMapping("/assignees")
    public Result<List<Map<String, Object>>> getAssignees() {
        List<Map<String, Object>> assignees = workOrderService.getAssignees();
        return Result.success(assignees);
    }
}

