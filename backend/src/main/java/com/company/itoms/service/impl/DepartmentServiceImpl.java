package com.company.itoms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.itoms.entity.DepartmentEntity;
import com.company.itoms.event.DepartmentDeleteEvent;
import com.company.itoms.mapper.DepartmentMapper;
import com.company.itoms.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, DepartmentEntity> implements DepartmentService {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(java.io.Serializable id) {
        boolean success = super.removeById(id);
        if (success) {
            eventPublisher.publishEvent(new DepartmentDeleteEvent(this, (Long) id));
        }
        return success;
    }
}

