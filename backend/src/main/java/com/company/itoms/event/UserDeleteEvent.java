package com.company.itoms.event;

import org.springframework.context.ApplicationEvent;

public class UserDeleteEvent extends ApplicationEvent {
    private final Long userId;

    public UserDeleteEvent(Object source, Long userId) {
        super(source);
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }
}
