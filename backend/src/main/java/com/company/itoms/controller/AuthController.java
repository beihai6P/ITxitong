package com.company.itoms.controller;

import com.company.itoms.common.Result;
import com.company.itoms.dto.request.LoginRequest;
import com.company.itoms.dto.response.LoginResponse;
import com.company.itoms.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.company.itoms.dto.request.WxLoginRequest;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Validated @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }
    
    @PostMapping("/wxLogin")
    public Result<LoginResponse> wxLogin(@Validated @RequestBody WxLoginRequest request) {
        return Result.success(authService.wxLogin(request));
    }
}
