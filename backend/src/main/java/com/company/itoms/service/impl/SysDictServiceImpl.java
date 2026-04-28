package com.company.itoms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.itoms.entity.SysDictEntity;
import com.company.itoms.mapper.SysDictMapper;
import com.company.itoms.service.ISysDictService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.itoms.entity.KnowledgeBaseEntity;
import com.company.itoms.mapper.KnowledgeBaseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SysDictServiceImpl extends ServiceImpl<SysDictMapper, SysDictEntity> implements ISysDictService {

    @Autowired
    private KnowledgeBaseMapper knowledgeBaseMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(java.io.Serializable id) {
        SysDictEntity dict = this.getById(id);
        if (dict != null && "ASSET_CATEGORY".equals(dict.getDictCode())) {
            LambdaQueryWrapper<KnowledgeBaseEntity> query = new LambdaQueryWrapper<>();
            query.eq(KnowledgeBaseEntity::getAssetCategoryId, id);
            if (knowledgeBaseMapper.selectCount(query) > 0) {
                throw new IllegalStateException("存在关联的知识库词条，无法删除此分类，需同步迁移或失效");
            }
        }
        return super.removeById(id);
    }
}
