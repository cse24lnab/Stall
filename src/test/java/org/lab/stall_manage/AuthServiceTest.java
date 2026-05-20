package org.lab.stall_manage;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lab.stall_manage.config.JwtProperties;
import org.lab.stall_manage.dto.LoginRequest;
import org.lab.stall_manage.dto.RegisterRequest;
import org.lab.stall_manage.exception.UserNotExistException;
import org.lab.stall_manage.mapper.UserMapper;
import org.lab.stall_manage.pojo.User;
import org.lab.stall_manage.pojo.enums.Status;
import org.lab.stall_manage.pojo.enums.UserRole;
import org.lab.stall_manage.service.impl.AuthServiceImpl;
import org.lab.stall_manage.utils.JwtToken;
import org.lab.stall_manage.vo.AuthResponse;
import org.lab.stall_manage.vo.LoginResponse;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void registerThrowsWhenRequestIsNull() {
        assertThrows(IllegalArgumentException.class, () -> authService.register(null));
    }

    @Test
    void registerThrowsWhenUsernameMissing() {
        RegisterRequest request = new RegisterRequest();
        request.setPassword("12345678");

        assertThrows(IllegalArgumentException.class, () -> authService.register(request));
    }

    @Test
    void registerThrowsWhenPasswordMissing() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("alice01");

        assertThrows(IllegalArgumentException.class, () -> authService.register(request));
    }

    @Test
    void registerEncodesPasswordAndCallsMapper() {
        RegisterRequest request = new RegisterRequest("alice01", "12345678", "Alice", "13800000000");
        when(passwordEncoder.encode("12345678")).thenReturn("encoded-password");
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(101);
            return null;
        }).when(userMapper).add(any(User.class));

        AuthResponse response = authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).add(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("encoded-password", savedUser.getPasswordHash());
        assertNotEquals("12345678", savedUser.getPasswordHash());
        assertEquals("alice01", savedUser.getUsername());
        assertEquals("Alice", savedUser.getNickname());
        assertEquals("13800000000", savedUser.getPhone());

        assertEquals(101, response.getId());
        assertEquals("alice01", response.getUsername());
        assertEquals("Alice", response.getNickname());
        assertEquals("13800000000", response.getPhone());
        assertEquals("ACTIVE", response.getStatus());
    }

    @Test
    void loginThrowsWhenUserDoesNotExist() {
        LoginRequest request = new LoginRequest("alice01", "12345678");
        when(userMapper.findByUsername("alice01")).thenReturn(null);

        assertThrows(UserNotExistException.class, () -> authService.login(request));
    }

    @Test
    void loginThrowsWhenPasswordDoesNotMatch() {
        LoginRequest request = new LoginRequest("alice01", "12345678");
        User user = createUser();
        when(userMapper.findByUsername("alice01")).thenReturn(user);
        when(passwordEncoder.matches("12345678", "encoded-password")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(request));
        assertEquals("账号或者密码错误", ex.getMessage());
    }

    @Test
    void loginReturnsResponseAndJwtClaimsWhenPasswordMatches() {
        LoginRequest request = new LoginRequest("alice01", "12345678");
        User user = createUser();
        when(userMapper.findByUsername("alice01")).thenReturn(user);
        when(passwordEncoder.matches("12345678", "encoded-password")).thenReturn(true);
        when(jwtProperties.getSecretKey()).thenReturn("test-secret-key-1234567890");
        when(jwtProperties.getTime()).thenReturn(60000L);

        LoginResponse response = authService.login(request);

        assertEquals("Bearer", response.getTokenType());
        assertEquals(60, response.getExpiresIn());
        assertEquals("alice01", response.getUser().getUsername());
        assertEquals("ACTIVE", response.getUser().getStatus());

        Claims claims = JwtToken.parseToken("test-secret-key-1234567890", response.getAccessToken());
        assertEquals(1, ((Number) claims.get("id")).intValue());
        assertEquals("alice01", claims.get("username"));
        assertEquals("ADMIN", claims.get("role"));
    }

    private User createUser() {
        User user = new User();
        user.setId(1);
        user.setUsername("alice01");
        user.setPasswordHash("encoded-password");
        user.setNickname("Alice");
        user.setPhone("13800000000");
        user.setRole(UserRole.ADMIN);
        user.setStatus(Status.ACTIVE);
        return user;
    }
}
