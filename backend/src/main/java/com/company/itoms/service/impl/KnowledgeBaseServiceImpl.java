package com.company.itoms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.itoms.entity.KnowledgeBaseEntity;
import com.company.itoms.mapper.KnowledgeBaseMapper;
import com.company.itoms.service.KnowledgeBaseService;
import org.springframework.stereotype.Service;

@Service
public class KnowledgeBaseServiceImpl extends ServiceImpl<KnowledgeBaseMapper, KnowledgeBaseEntity> implements KnowledgeBaseService {
}
