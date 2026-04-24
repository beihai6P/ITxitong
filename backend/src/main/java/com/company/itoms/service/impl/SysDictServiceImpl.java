package com.company.itoms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.itoms.entity.SysDictEntity;
import com.company.itoms.mapper.SysDictMapper;
import com.company.itoms.service.ISysDictService;
import org.springframework.stereotype.Service;

@Service
public class SysDictServiceImpl extends ServiceImpl<SysDictMapper, SysDictEntity> implements ISysDictService {
}
