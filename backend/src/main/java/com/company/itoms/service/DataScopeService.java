package com.company.itoms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.company.itoms.entity.DataScopeEntity;
import java.util.List;

public interface DataScopeService extends IService<DataScopeEntity> {

    List<DataScopeEntity> getByRoleId(Long roleId);

    void saveByRoleId(Long roleId, List<DataScopeEntity> dataScopes);

    void deleteByRoleId(Long roleId);

    String buildDataScopeSql(Long userId, String dataScope, Long deptId);
}
