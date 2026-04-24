package com.company.itoms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.itoms.entity.KnowledgeBaseEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface KnowledgeBaseMapper extends BaseMapper<KnowledgeBaseEntity> {
    @org.apache.ibatis.annotations.Select("SELECT * FROM knowledge_base WHERE is_deleted = 1")
    java.util.List<com.company.itoms.entity.KnowledgeBaseEntity> selectDeleted();

    @org.apache.ibatis.annotations.Update("UPDATE knowledge_base SET is_deleted = 0 WHERE id = #{id}")
    int restoreById(@org.apache.ibatis.annotations.Param("id") Long id);

}
