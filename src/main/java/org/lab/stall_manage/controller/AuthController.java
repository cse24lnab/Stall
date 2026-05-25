package org.lab.stall_manage.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.lab.stall_manage.dto.LoginRequest;
import org.lab.stall_manage.dto.RegisterRequest;
import org.lab.stall_manage.pojo.Result;
import org.lab.stall_manage.service.AuthService;
import org.lab.stall_manage.vo.AuthResponse;
import org.lab.stall_manage.vo.LoginResponse;
import org.lab.stall_manage.vo.MeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/auth/register")
    public Result<AuthResponse> register(@RequestBody @Valid RegisterRequest registerRequest)
    {
        return Result.success(authService.register(registerRequest));
    }

    @PostMapping("/auth/login")
    public Result<LoginResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
         return Result.success(authService.login(loginRequest));
    }


}
