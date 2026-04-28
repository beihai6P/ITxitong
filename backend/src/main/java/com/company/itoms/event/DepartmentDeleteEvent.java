package com.company.itoms.event;

import org.springframework.context.ApplicationEvent;

public class DepartmentDeleteEvent extends ApplicationEvent {
    private final Long deptId;

    public DepartmentDeleteEvent(Object source, Long deptId) {
        super(source);
        this.deptId = deptId;
    }

    public Long getDeptId() {
        return deptId;
    }
}
