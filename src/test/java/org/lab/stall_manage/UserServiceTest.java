package org.lab.stall_manage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lab.stall_manage.context.BaseContext;
import org.lab.stall_manage.context.CurrentUser;
import org.lab.stall_manage.dto.ChangePasswordRequest;
import org.lab.stall_manage.dto.UpdateMeRequest;
import org.lab.stall_manage.exception.UserNotExistException;
import org.lab.stall_manage.mapper.UserMapper;
import org.lab.stall_manage.pojo.User;
import org.lab.stall_manage.pojo.enums.Status;
import org.lab.stall_manage.pojo.enums.UserRole;
import org.lab.stall_manage.service.impl.UserServiceImpl;
import org.lab.stall_manage.vo.MeResponse;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    //必须是实体类
    private UserServiceImpl userService;

    @BeforeEach
    void setup()
    {
        CurrentUser currentUser = new CurrentUser();
        currentUser.setId(1);
        BaseContext.setCurrentUser(currentUser);
    }

    @AfterEach
    void remove()
    {
        BaseContext.RemoveCurrentUser();
    }

    @Test
    void findMeThrowsWhenCurrentUserMissing()
    {
        BaseContext.RemoveCurrentUser();

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.findMe());

        assertEquals("登录过期", ex.getMessage());
    }

    @Test
    void findMeThrowsWhenCurrentUserIdIsNull()
    {
        BaseContext.setCurrentUser(new CurrentUser());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.findMe());

        assertEquals("登录过期", ex.getMessage());
    }

    @Test
    void findMeNotFoundReturnEmpty()
    {
        when(userMapper.find(1)).thenReturn(null);

        Optional<MeResponse> me = userService.findMe();

        assertTrue(me.isEmpty());
    }

    @Test
    void findMeSuccess()
    {
        User user = createUser();
        when(userMapper.find(1)).thenReturn(user);

        Optional<MeResponse> me = userService.findMe();

        assertTrue(me.isPresent());
        MeResponse meResponse = me.get();
        assertEquals(1, meResponse.getId());
        assertEquals("testuser", meResponse.getUsername());
        assertEquals("Test User", meResponse.getNickname());
        assertEquals("1234567890", meResponse.getPhone());
        assertEquals(1, meResponse.getAvatarFileId());
        assertEquals("http://example.com/avatar.jpg", meResponse.getAvatarUrl());
        assertEquals(UserRole.USER, meResponse.getRole());
        assertEquals(Status.ACTIVE.name(), meResponse.getStatus());
        assertEquals(user.getCreateTime(), meResponse.getCreateTime());
        assertEquals(user.getUpdateTime(), meResponse.getUpdateTime());
    }

    @Test
    void updateMeParamNullThrowIllegalArgumentEx()
    {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.updateMe(null));

        assertEquals("参数不能为空", ex.getMessage());
        verify(userMapper, never()).update(any(User.class));
    }

    @Test
    void updateMeThrowsWhenAllFieldsNull()
    {
        UpdateMeRequest updateMeRequest = new UpdateMeRequest();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.updateMe(updateMeRequest));

        assertEquals("参数不能为空", ex.getMessage());
        verify(userMapper, never()).update(any(User.class));
    }

    @Test
    void updateMeThrowsWhenAllFieldsBlank()
    {
        UpdateMeRequest updateMeRequest = new UpdateMeRequest("", " ", null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.updateMe(updateMeRequest));

        assertEquals("参数不能为空", ex.getMessage());
        verify(userMapper, never()).update(any(User.class));
    }

    @Test
    void updateMeThrowsWhenCurrentUserMissing()
    {
        BaseContext.RemoveCurrentUser();
        UpdateMeRequest updateMeRequest = new UpdateMeRequest("test", "13800000000", 1);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.updateMe(updateMeRequest));

        assertEquals("登录过期", ex.getMessage());
        verify(userMapper, never()).update(any(User.class));
    }

    @Test
    void updateMeSuccess()
    {
        UpdateMeRequest updateMeRequest = new UpdateMeRequest("test", "13800000000", 1);

        userService.updateMe(updateMeRequest);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).update(captor.capture());
        User captureUser = captor.getValue();
        assertEquals(1, captureUser.getId());
        assertEquals("test", captureUser.getNickname());
        assertEquals("13800000000", captureUser.getPhone());
        assertEquals(1, captureUser.getAvatarFileId());
    }

    @Test
    void changeNullPwdThrowIllegalArgumentEx()
    {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.changePassword(null));

        assertEquals("参数不能为空", ex.getMessage());
        verify(userMapper, never()).update(any(User.class));
    }

    @Test
    void changePasswordThrowsWhenCurrentUserMissing()
    {
        BaseContext.RemoveCurrentUser();
        ChangePasswordRequest request = new ChangePasswordRequest("oldPassword", "newPassword");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.changePassword(request));

        assertEquals("登录过期", ex.getMessage());
        verify(userMapper, never()).update(any(User.class));
    }

    @Test
    void changeNotExistsUserPwdThrowUserNotExistEx()
    {
        ChangePasswordRequest request = new ChangePasswordRequest("oldPassword", "newPassword");
        when(userMapper.find(1)).thenReturn(null);

        UserNotExistException ex = assertThrows(UserNotExistException.class, () -> userService.changePassword(request));

        assertEquals("用户不存在", ex.getMessage());
        verify(userMapper, never()).update(any(User.class));
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void changeNotMatchPwdThrowRuntimeEx()
    {
        User user = new User();
        user.setPasswordHash("encoded-old-password");
        when(userMapper.find(1)).thenReturn(user);
        when(passwordEncoder.matches("oldPassword", "encoded-old-password")).thenReturn(false);

        ChangePasswordRequest request = new ChangePasswordRequest("oldPassword", "newPassword");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.changePassword(request));

        assertEquals("密码错误", ex.getMessage());
        verify(userMapper, never()).update(any(User.class));
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void changePasswordSuccess()
    {
        User user = new User();
        user.setPasswordHash("encoded-old-password");
        user.setId(1);
        when(userMapper.find(1)).thenReturn(user);
        when(passwordEncoder.matches("oldPassword", "encoded-old-password")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("encoded-new-password");

        ChangePasswordRequest request = new ChangePasswordRequest("oldPassword", "newPassword");

        userService.changePassword(request);

        verify(passwordEncoder).matches("oldPassword", "encoded-old-password");
        verify(passwordEncoder).encode("newPassword");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).update(captor.capture());
        User updatedUser = captor.getValue();
        assertEquals(1, updatedUser.getId());
        assertEquals("encoded-new-password", updatedUser.getPasswordHash());
    }

    private User createUser()
    {
        User user = new User();
        user.setId(1);
        user.setUsername("testuser");
        user.setNickname("Test User");
        user.setPhone("1234567890");
        user.setAvatarFileId(1);
        user.setAvatarUrl("http://example.com/avatar.jpg");
        user.setRole(UserRole.USER);
        user.setStatus(Status.ACTIVE);
        user.setCreateTime(LocalDateTime.of(2026, 5, 27, 20, 0));
        user.setUpdateTime(LocalDateTime.of(2026, 5, 27, 20, 5));
        return user;
    }
}
