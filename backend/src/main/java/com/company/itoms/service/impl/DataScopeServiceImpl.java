package com.company.itoms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.itoms.entity.DataScopeEntity;
import com.company.itoms.entity.UserEntity;
import com.company.itoms.mapper.DataScopeMapper;
import com.company.itoms.mapper.UserMapper;
import com.company.itoms.service.DataScopeService;
import com.company.itoms.service.RoleService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DataScopeServiceImpl extends ServiceImpl<DataScopeMapper, DataScopeEntity> implements DataScopeService {

    private final UserMapper userMapper;
    private final RoleService roleService;

    @Override
    public List<DataScopeEntity> getByRoleId(Long roleId) {
        return baseMapper.selectByRoleId(roleId);
    }

    @Override
    @Transactional
    public void saveByRoleId(Long roleId, List<DataScopeEntity> dataScopes) {
        baseMapper.deleteByRoleId(roleId);
        if (dataScopes != null && !dataScopes.isEmpty()) {
            for (DataScopeEntity ds : dataScopes) {
                ds.setRoleId(roleId);
                baseMapper.insert(ds);
            }
        }
    }

    @Override
    @Transactional
    public void deleteByRoleId(Long roleId) {
        baseMapper.deleteByRoleId(roleId);
    }

    @Override
    public String buildDataScopeSql(Long userId, String dataScope, Long deptId) {
        if (userId == null) {
            return "";
        }

        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            return "";
        }

        if ("SUPER_ADMIN".equalsIgnoreCase(user.getRole()) || "admin".equalsIgnoreCase(user.getUsername())) {
            return "";
        }

        if ("ALL".equals(dataScope)) {
            return "";
        }

        if ("SELF".equals(dataScope)) {
            return "creator_id = " + userId;
        }

        if ("DEPT".equals(dataScope) && deptId != null) {
            return "department_id = " + deptId;
        }

        if ("DEPT_AND_CHILD".equals(dataScope) && deptId != null) {
            // 这里需要根据实际的部门表结构实现递归查询子部门
            // 暂时返回本部门查询，后续需要完善
            return "department_id = " + deptId;
        }

        if ("CUSTOM".equals(dataScope)) {
            // 从角色的dataScopeDeptIds字段获取自定义部门ID
            return "";
        }

        return "";
    }
}
