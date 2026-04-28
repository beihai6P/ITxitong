package com.company.itoms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.company.itoms.entity.RoleMenuEntity;
import java.util.List;

public interface RoleMenuService extends IService<RoleMenuEntity> {

    List<Long> getMenuIdsByRoleId(Long roleId);

    void saveRoleMenus(Long roleId, List<Long> menuIds);

    void deleteByRoleId(Long roleId);
}
