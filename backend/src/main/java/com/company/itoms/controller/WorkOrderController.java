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
import com.company.itoms.service.WorkOrderService;
import com.company.itoms.util.HtmlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
}
