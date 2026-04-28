package com.company.itoms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.itoms.entity.RoleMenuEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface RoleMenuMapper extends BaseMapper<RoleMenuEntity> {

    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);

    void deleteByRoleId(@Param("roleId") Long roleId);

    void batchInsert(@Param("roleId") Long roleId, @Param("menuIds") List<Long> menuIds);
}
