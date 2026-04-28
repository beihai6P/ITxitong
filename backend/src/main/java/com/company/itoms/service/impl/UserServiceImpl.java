package com.company.itoms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.itoms.entity.UserEntity;
import com.company.itoms.event.UserDeleteEvent;
import com.company.itoms.event.UserStatusChangeEvent;
import com.company.itoms.mapper.UserMapper;
import com.company.itoms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(UserEntity entity) {
        UserEntity oldUser = this.getById(entity.getId());
        boolean success = super.updateById(entity);
        if (success && oldUser != null) {
            UserEntity newUser = this.getById(entity.getId());
            eventPublisher.publishEvent(new UserStatusChangeEvent(this, oldUser, newUser));
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(java.io.Serializable id) {
        boolean success = super.removeById(id);
        if (success) {
            eventPublisher.publishEvent(new UserDeleteEvent(this, (Long) id));
        }
        return success;
    }
}
