package com.company.itoms.aspect;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.itoms.common.DataScope;
import com.company.itoms.common.DataScopeContextHolder;
import com.company.itoms.entity.UserEntity;
import com.company.itoms.mapper.UserMapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class DataScopeAspect {

    @Autowired
    private UserMapper userMapper;

    @Before("@annotation(dataScope)")
    public void before(JoinPoint joinPoint, DataScope dataScope) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return;
        }

        String username = authentication.getName();
        if (username == null || username.isEmpty() || "anonymousUser".equals(username)) {
            return;
        }

        UserEntity user = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUsername, username));

        if (user != null) {
            // 如果是超级管理员则不过滤
            if ("admin".equalsIgnoreCase(user.getRole())) {
                DataScopeContextHolder.setSqlFilter("");
                return;
            }

            if (user.getDepartmentId() != null) {
                String deptAlias = dataScope.deptAlias();
                String prefix = (deptAlias == null || deptAlias.trim().isEmpty()) ? "" : deptAlias + ".";
                String sqlFilter = prefix + "department_id = " + user.getDepartmentId();
                DataScopeContextHolder.setSqlFilter(sqlFilter);
            }
        }
    }

    @After("@annotation(dataScope)")
    public void after(JoinPoint joinPoint, DataScope dataScope) {
        DataScopeContextHolder.clear();
    }
}
