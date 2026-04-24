package com.company.itoms.service;

import com.company.itoms.dto.request.LoginRequest;
import com.company.itoms.dto.request.WxLoginRequest;
import com.company.itoms.dto.response.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    
    LoginResponse wxLogin(WxLoginRequest request);
}
