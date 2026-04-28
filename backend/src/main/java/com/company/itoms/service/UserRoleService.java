package com.company.itoms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.company.itoms.entity.UserRoleEntity;
import java.util.List;

public interface UserRoleService extends IService<UserRoleEntity> {

    List<Long> getRoleIdsByUserId(Long userId);

    List<String> getRoleCodesByUserId(Long userId);

    List<Long> getMenuIdsByRoleId(Long roleId);

    List<String> getPermissionsByUserId(Long userId);

    void assignRoles(Long userId, List<Long> roleIds);

    void removeAllRoles(Long userId);

    void bindMenuToRole(Long roleId, List<Long> menuIds);

    void removeMenuFromRole(Long roleId);
}
