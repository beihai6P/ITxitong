package com.company.itoms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.company.itoms.common.DataScope;
import com.company.itoms.entity.AssetEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.io.Serializable;
import java.util.List;

@Mapper
public interface AssetMapper extends BaseMapper<AssetEntity> {

    @DataScope
    @Override
    List<AssetEntity> selectList(@Param(Constants.WRAPPER) Wrapper<AssetEntity> queryWrapper);

    @DataScope
    @Override
    <E extends IPage<AssetEntity>> E selectPage(E page, @Param(Constants.WRAPPER) Wrapper<AssetEntity> queryWrapper);

    @DataScope
    @Override
    AssetEntity selectOne(@Param(Constants.WRAPPER) Wrapper<AssetEntity> queryWrapper);

    @DataScope
    @Override
    AssetEntity selectById(Serializable id);
    @org.apache.ibatis.annotations.Select("SELECT * FROM asset WHERE is_deleted = 1")
    java.util.List<com.company.itoms.entity.AssetEntity> selectDeleted();

    @org.apache.ibatis.annotations.Update("UPDATE asset SET is_deleted = 0 WHERE id = #{id}")
    int restoreById(@org.apache.ibatis.annotations.Param("id") Long id);

}
