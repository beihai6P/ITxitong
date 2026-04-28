package com.company.itoms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.itoms.entity.UserRoleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserRoleMapper extends BaseMapper<UserRoleEntity> {

    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);

    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);

    void deleteByUserId(@Param("userId") Long userId);

    void deleteByRoleId(@Param("roleId") Long roleId);
}
