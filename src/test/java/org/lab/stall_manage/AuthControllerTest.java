package org.lab.stall_manage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lab.stall_manage.controller.AuthController;
import org.lab.stall_manage.dto.LoginRequest;
import org.lab.stall_manage.dto.RegisterRequest;
import org.lab.stall_manage.exception.GlobalException;
import org.lab.stall_manage.exception.UserNotExistException;
import org.lab.stall_manage.service.AuthService;
import org.lab.stall_manage.vo.AuthResponse;
import org.lab.stall_manage.vo.LoginResponse;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthService authService;

    //伪装postman
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AuthController controller = new AuthController();
        ReflectionTestUtils.setField(controller, "authService", authService);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalException())
                .setValidator(validator)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void registerReturnsSuccess() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(new AuthResponse(101, "alice01", "Alice", "13800000000", "ACTIVE"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"alice01","password":"12345678","nickname":"Alice","phone":"13800000000"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.username").value("alice01"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void loginReturnsSuccess() throws Exception {
        LoginResponse response = new LoginResponse();
        response.setAccessToken("jwt-token");
        response.setTokenType("Bearer");
        response.setExpiresIn(60);
        response.setUser(new AuthResponse(101, "alice01", "Alice", "13800000000", "ACTIVE"));
        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"alice01","password":"12345678"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.user.username").value("alice01"));
    }

    @Test
    void registerRejectsMissingUsername() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"password":"12345678","nickname":"Alice","phone":"13800000000"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("用户名不能为空"));
    }

    @Test
    void loginRejectsMissingPassword() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"alice01"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("密码不能为空"));
    }

    @Test
    void loginReturnsUserNotExistMessageWhenServiceThrows() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenThrow(new UserNotExistException("用户不存在"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"alice01","password":"12345678"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("用户不存在"));
    }

    @Test
    void loginReturnsRuntimeExceptionMessageWhenServiceThrows() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenThrow(new RuntimeException("账号或者密码错误"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"alice01","password":"12345678"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("账号或者密码错误"));
    }
}
