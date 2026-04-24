package com.company.itoms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.company.itoms.common.DataScope;
import com.company.itoms.entity.WorkOrderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.io.Serializable;
import java.util.List;

@Mapper
public interface WorkOrderMapper extends BaseMapper<WorkOrderEntity> {

    @DataScope
    @Override
    List<WorkOrderEntity> selectList(@Param(Constants.WRAPPER) Wrapper<WorkOrderEntity> queryWrapper);

    @DataScope
    @Override
    <E extends IPage<WorkOrderEntity>> E selectPage(E page, @Param(Constants.WRAPPER) Wrapper<WorkOrderEntity> queryWrapper);

    @DataScope
    @Override
    WorkOrderEntity selectOne(@Param(Constants.WRAPPER) Wrapper<WorkOrderEntity> queryWrapper);

    @DataScope
    @Override
    WorkOrderEntity selectById(Serializable id);
    @org.apache.ibatis.annotations.Select("SELECT * FROM work_order WHERE is_deleted = 1")
    java.util.List<com.company.itoms.entity.WorkOrderEntity> selectDeleted();

    @org.apache.ibatis.annotations.Update("UPDATE work_order SET is_deleted = 0 WHERE id = #{id}")
    int restoreById(@org.apache.ibatis.annotations.Param("id") Long id);

}
