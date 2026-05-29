package org.lab.stall_manage.service;

import org.apache.tomcat.websocket.AuthenticationException;
import org.lab.stall_manage.dto.LoginRequest;
import org.lab.stall_manage.dto.RegisterRequest;
import org.lab.stall_manage.vo.AuthResponse;
import org.lab.stall_manage.vo.LoginResponse;
import org.lab.stall_manage.vo.MeResponse;
import org.springframework.stereotype.Service;

import java.util.Optional;

public interface AuthService {
    /**
     * 注册用户
     * @throws IllegalArgumentException 参数不合法
     */
    AuthResponse register(RegisterRequest registerRequest);

    /**
     * 用户登录
     * @throws org.lab.stall_manage.exception.UserNotExistException 用户不存在
     * @throws RuntimeException 账号或者密码错误
     */
    LoginResponse login(LoginRequest loginRequest);

}
