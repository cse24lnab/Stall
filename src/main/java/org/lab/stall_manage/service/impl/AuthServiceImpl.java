package org.lab.stall_manage.service.impl;

import org.lab.stall_manage.config.JwtProperties;
import org.lab.stall_manage.dto.LoginRequest;
import org.lab.stall_manage.dto.RegisterRequest;
import org.lab.stall_manage.exception.UserNotExistException;
import org.lab.stall_manage.mapper.UserMapper;
import org.lab.stall_manage.pojo.User;
import org.lab.stall_manage.service.AuthService;
import org.lab.stall_manage.utils.JwtToken;
import org.lab.stall_manage.vo.AuthResponse;
import org.lab.stall_manage.vo.LoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public AuthResponse register(RegisterRequest registerRequest)
    {
        //defend
        if(!checkRegister(registerRequest))
        {
            throw new IllegalArgumentException("参数不合法");
        }
        String pwdHash=passwordEncoder.encode(registerRequest.getPassword());
        User user=new User();
        user.setUsername(registerRequest.getUsername());
        user.setPasswordHash(pwdHash);
        user.setNickname(registerRequest.getNickname());
        user.setPhone(registerRequest.getPhone());
        userMapper.add(user);
        //登录默认激活，主要是不知道不默认怎么写..
        return new AuthResponse(user.getId(),user.getUsername(),user.getNickname(),user.getPhone(),"ACTIVE");
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        User user = userMapper.findByUsername(loginRequest.getUsername());
        if(user == null)
        {
            throw new UserNotExistException("用户不存在");
        }
        boolean matches = passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash());
        if(matches)
        {
            return this.getLoginResponse(user);
        }
        //todo 自定义异常
        throw new RuntimeException("账号或者密码错误");
    }

    private boolean checkRegister(RegisterRequest registerRequest) {
        if (registerRequest == null) {
            return false;
        }
        if (registerRequest.getUsername() == null || registerRequest.getUsername().isBlank()) {
            return false;
        }
        return registerRequest.getPassword() != null && !registerRequest.getPassword().isBlank();
    }

    private LoginResponse getLoginResponse(User user)
    {
        LoginResponse loginResponse=new LoginResponse();
        loginResponse.setUser(new AuthResponse(
                user.getId(),user.getUsername(),user.getNickname(),user.getPhone(),user.getStatus().name()));
        Map<String,Object> claims = new HashMap<>();
        claims.put("id",user.getId());
        claims.put("username",user.getUsername());
        claims.put("role",user.getRole().name());
        String token = JwtToken.createToken(jwtProperties.getSecretKey(), jwtProperties.getTime(), claims);
        loginResponse.setAccessToken(token);
        loginResponse.setTokenType("Bearer");
        loginResponse.setExpiresIn((int)(jwtProperties.getTime() /1000));
        return loginResponse;
    }
}
