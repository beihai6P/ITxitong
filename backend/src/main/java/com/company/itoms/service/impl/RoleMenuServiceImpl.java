package com.company.itoms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.itoms.entity.RoleMenuEntity;
import com.company.itoms.mapper.RoleMenuMapper;
import com.company.itoms.service.RoleMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleMenuServiceImpl extends ServiceImpl<RoleMenuMapper, RoleMenuEntity> implements RoleMenuService {

    @Override
    public List<Long> getMenuIdsByRoleId(Long roleId) {
        return baseMapper.selectMenuIdsByRoleId(roleId);
    }

    @Override
    @Transactional
    public void saveRoleMenus(Long roleId, List<Long> menuIds) {
        baseMapper.deleteByRoleId(roleId);
        if (menuIds != null && !menuIds.isEmpty()) {
            baseMapper.batchInsert(roleId, menuIds);
        }
    }

    @Override
    @Transactional
    public void deleteByRoleId(Long roleId) {
        baseMapper.deleteByRoleId(roleId);
    }
}
