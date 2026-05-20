package org.lab.stall_manage.service;

import org.lab.stall_manage.dto.LoginRequest;
import org.lab.stall_manage.dto.RegisterRequest;
import org.lab.stall_manage.vo.AuthResponse;
import org.lab.stall_manage.vo.LoginResponse;
import org.springframework.stereotype.Service;

public interface AuthService {
    /**
     * 注册用户
     */
    AuthResponse register(RegisterRequest registerRequest);

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest loginRequest);
}
