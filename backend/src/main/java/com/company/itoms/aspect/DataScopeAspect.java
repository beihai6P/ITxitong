package com.company.itoms.aspect;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.itoms.common.DataScope;
import com.company.itoms.common.DataScopeContextHolder;
import com.company.itoms.entity.RoleEntity;
import com.company.itoms.entity.UserEntity;
import com.company.itoms.mapper.RoleMapper;
import com.company.itoms.mapper.UserMapper;
import com.company.itoms.service.DataScopeService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Aspect
@Component
public class DataScopeAspect {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private DataScopeService dataScopeService;

    @Before("@annotation(dataScopeAnnotation)")
    public void before(JoinPoint joinPoint, DataScope dataScopeAnnotation) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return;
        }

        String username = authentication.getName();
        if (username == null || username.isEmpty() || "anonymousUser".equals(username)) {
            return;
        }

        UserEntity user = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUsername, username)
                .eq(UserEntity::getIsDeleted, 0));

        if (user != null) {
            if ("admin".equalsIgnoreCase(username)) {
                DataScopeContextHolder.setSqlFilter("");
                return;
            }

            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            List<String> roleCodes = authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role) // 去掉"ROLE_"前缀
                    .collect(Collectors.toList());

            if (roleCodes.contains("SUPER_ADMIN")) {
                DataScopeContextHolder.setSqlFilter("");
                return;
            }

            List<RoleEntity> userRoles = roleMapper.selectList(new LambdaQueryWrapper<RoleEntity>()
                    .in(RoleEntity::getRoleCode, roleCodes)
                    .eq(RoleEntity::getIsDeleted, 0));

            boolean hasAllScope = userRoles.stream()
                    .anyMatch(r -> "ALL".equals(r.getDataScope()));

            if (hasAllScope) {
                DataScopeContextHolder.setSqlFilter("");
                return;
            }

            String scope = "SELF";
            for (RoleEntity role : userRoles) {
                if (role.getDataScope() != null && !"".equals(role.getDataScope())) {
                    scope = role.getDataScope();
                    break;
                }
            }

            String sqlFilter = dataScopeService.buildDataScopeSql(user.getId(), scope, user.getDepartmentId());
            DataScopeContextHolder.setSqlFilter(sqlFilter);
        }
    }

    @After("@annotation(dataScopeAnnotation)")
    public void after(JoinPoint joinPoint, DataScope dataScopeAnnotation) {
        DataScopeContextHolder.clear();
    }
}
