package com.company.itoms.service.impl;

import com.company.itoms.dto.request.LoginRequest;
import com.company.itoms.dto.request.WxLoginRequest;
import com.company.itoms.dto.response.LoginResponse;
import com.company.itoms.entity.UserEntity;
import com.company.itoms.service.AuthService;
import com.company.itoms.service.MenuService;
import com.company.itoms.service.UserRoleService;
import com.company.itoms.service.UserService;
import com.company.itoms.util.JwtUtil;
import com.company.itoms.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import okhttp3.*;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final UserDetailsService userDetailsService;
    private final MenuService menuService;
    private final UserRoleService userRoleService;

    @Value("${wx.appId:dummy_appid}")
    private String wxAppId;

    @Value("${wx.appSecret:dummy_secret}")
    private String wxAppSecret;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build();

    @Override
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Map<String, Object> claims = new HashMap<>();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        String role = roles.stream()
                .findFirst()
                .orElse("ROLE_USER");
        claims.put("role", role);

        UserEntity user = userService.getOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUsername, userDetails.getUsername()));

        List<String> permissions = menuService.getPermissionsByUserId(user.getId());

        String token = jwtUtil.generateToken(userDetails.getUsername(), claims);

        return LoginResponse.builder()
                .token(token)
                .username(userDetails.getUsername())
                .role(role)
                .userId(user.getId())
                .roles(roles)
                .permissions(permissions)
                .build();
    }

    @Override
    public LoginResponse wxLogin(WxLoginRequest request) {
        String url = String.format("https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                wxAppId, wxAppSecret, request.getCode());

        Request httpRequest = new Request.Builder().url(url).get().build();
        String openId = null;

        try (Response response = httpClient.newCall(httpRequest).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                JSONObject jsonObject = JSON.parseObject(responseBody);
                if (jsonObject.containsKey("openid")) {
                    openId = jsonObject.getString("openid");
                } else {
                    log.error("WeChat API error: {}", responseBody);
                    throw new BusinessException(500, "获取微信OpenID失败");
                }
            } else {
                throw new BusinessException(500, "调用微信接口失败");
            }
        } catch (IOException e) {
            log.error("Failed to call WeChat API", e);
            throw new BusinessException(500, "调用微信接口异常");
        }

        UserEntity user = userService.getOne(new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getOpenId, openId).last("LIMIT 1"));

        if (user != null) {
            return generateLoginResponse(user.getUsername());
        } else {
            if (request.getUsername() == null || request.getPassword() == null) {
                throw new BusinessException(401, "该微信号未绑定账号，请提供用户名和密码进行绑定");
            }

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            UserEntity existingUser = userService.getOne(new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getUsername, request.getUsername()));
            if (existingUser != null) {
                existingUser.setOpenId(openId);
                userService.updateById(existingUser);
                return generateLoginResponse(existingUser.getUsername());
            } else {
                throw new BusinessException(404, "用户不存在");
            }
        }
    }

    private LoginResponse generateLoginResponse(String username) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        Map<String, Object> claims = new HashMap<>();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        String role = roles.stream()
                .findFirst()
                .orElse("ROLE_USER");
        claims.put("role", role);

        UserEntity user = userService.getOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUsername, username));

        List<String> permissions = menuService.getPermissionsByUserId(user.getId());

        String token = jwtUtil.generateToken(username, claims);

        return LoginResponse.builder()
                .token(token)
                .username(username)
                .role(role)
                .userId(user.getId())
                .roles(roles)
                .permissions(permissions)
                .build();
    }
}
