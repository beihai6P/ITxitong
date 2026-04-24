package com.company.itoms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.company.itoms.entity.SysDictEntity;

import java.util.List;

public interface SysDictService extends IService<SysDictEntity> {
    
    List<SysDictEntity> getDictByCode(String dictCode);
    
}
