package com.company.itoms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.itoms.entity.DataScopeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface DataScopeMapper extends BaseMapper<DataScopeEntity> {

    List<DataScopeEntity> selectByRoleId(@Param("roleId") Long roleId);

    void deleteByRoleId(@Param("roleId") Long roleId);
}
