package com.company.itoms.event;

import com.company.itoms.entity.WorkOrderEntity;
import org.springframework.context.ApplicationEvent;

public class WorkOrderEvaluateEvent extends ApplicationEvent {
    private final WorkOrderEntity workOrder;

    public WorkOrderEvaluateEvent(Object source, WorkOrderEntity workOrder) {
        super(source);
        this.workOrder = workOrder;
    }

    public WorkOrderEntity getWorkOrder() {
        return workOrder;
    }
}
