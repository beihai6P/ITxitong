package com.company.itoms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.itoms.entity.SysConfigEntity;
import com.company.itoms.mapper.SysConfigMapper;
import com.company.itoms.service.ISysConfigService;
import org.springframework.stereotype.Service;

@Service
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfigEntity> implements ISysConfigService {
}
