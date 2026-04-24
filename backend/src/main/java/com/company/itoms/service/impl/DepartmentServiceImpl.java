package com.company.itoms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.itoms.entity.DepartmentEntity;
import com.company.itoms.mapper.DepartmentMapper;
import com.company.itoms.service.DepartmentService;
import org.springframework.stereotype.Service;

@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, DepartmentEntity> implements DepartmentService {
}
