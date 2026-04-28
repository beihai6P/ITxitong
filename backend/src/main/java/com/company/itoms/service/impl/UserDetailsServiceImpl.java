package com.company.itoms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.itoms.entity.RoleEntity;
import com.company.itoms.entity.UserEntity;
import com.company.itoms.entity.UserRoleEntity;
import com.company.itoms.mapper.RoleMapper;
import com.company.itoms.mapper.UserMapper;
import com.company.itoms.mapper.UserRoleMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;

    @Data
    public static class CustomUserDetails extends User {
        private Long id;

        public CustomUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities, Long id) {
            super(username, password, authorities);
            this.id = id;
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUsername, username)
                .eq(UserEntity::getIsDeleted, 0));

        if (userEntity == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        if ("admin".equalsIgnoreCase(username)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            authorities.add(new SimpleGrantedAuthority("ROLE_ENGINEER"));
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        } else {
            List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userEntity.getId());

            if (roleIds != null && !roleIds.isEmpty()) {
                List<RoleEntity> roles = roleMapper.selectBatchIds(roleIds);
                authorities = roles.stream()
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getRoleCode().toUpperCase()))
                        .collect(Collectors.toList());
            }

            if (authorities.isEmpty()) {
                String legacyRole = userEntity.getRole() != null ?
                        "ROLE_" + userEntity.getRole().toUpperCase() : "ROLE_USER";
                authorities.add(new SimpleGrantedAuthority(legacyRole));
            }
        }

        return new CustomUserDetails(
                userEntity.getUsername(),
                userEntity.getPassword(),
                authorities,
                userEntity.getId()
        );
    }
}