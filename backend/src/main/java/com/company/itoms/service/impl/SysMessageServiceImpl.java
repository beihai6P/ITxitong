package com.company.itoms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.itoms.entity.SysMessageEntity;
import com.company.itoms.mapper.SysMessageMapper;
import com.company.itoms.service.ISysMessageService;
import org.springframework.stereotype.Service;

@Service
public class SysMessageServiceImpl extends ServiceImpl<SysMessageMapper, SysMessageEntity> implements ISysMessageService {
}
