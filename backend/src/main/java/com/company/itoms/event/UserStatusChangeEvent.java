package com.company.itoms.event;

import com.company.itoms.entity.UserEntity;
import org.springframework.context.ApplicationEvent;

public class UserStatusChangeEvent extends ApplicationEvent {
    private final UserEntity oldUser;
    private final UserEntity newUser;

    public UserStatusChangeEvent(Object source, UserEntity oldUser, UserEntity newUser) {
        super(source);
        this.oldUser = oldUser;
        this.newUser = newUser;
    }

    public UserEntity getOldUser() {
        return oldUser;
    }

    public UserEntity getNewUser() {
        return newUser;
    }
}
