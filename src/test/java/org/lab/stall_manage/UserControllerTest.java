package org.lab.stall_manage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lab.stall_manage.controller.UserController;
import org.lab.stall_manage.dto.ChangePasswordRequest;
import org.lab.stall_manage.dto.UpdateMeRequest;
import org.lab.stall_manage.exception.GlobalException;
import org.lab.stall_manage.exception.UserNotExistException;
import org.lab.stall_manage.pojo.enums.UserRole;
import org.lab.stall_manage.service.UserService;
import org.lab.stall_manage.vo.FileResponse;
import org.lab.stall_manage.vo.MeResponse;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Mock
    private UserService userService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp()
    {
        UserController userController = new UserController();
        ReflectionTestUtils.setField(userController, "userService", userService);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setControllerAdvice(new GlobalException())
                .setValidator(validator)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void findMeSuccess() throws Exception
    {
        MeResponse meResponse = createResponse();
        when(userService.findMe()).thenReturn(Optional.of(meResponse));

        mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.nickname").value("Alice"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("alice01"));
    }

    @Test
    void findMeReturnsNullDataWhenServiceReturnsEmpty() throws Exception
    {
        when(userService.findMe()).thenReturn(Optional.empty());

        mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void updateMeSuccess() throws Exception
    {
        mockMvc.perform(put("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\": \"13413441344\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        verify(userService).updateMe(any(UpdateMeRequest.class));
    }

    @Test
    void updateMeWithWrongPhoneThrowsEx() throws Exception
    {
        mockMvc.perform(put("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\": \"33413441344\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("手机号格式错误"));
    }

    @Test
    void updateMeReturnsLoginExpiredMessageWhenServiceThrows() throws Exception
    {
        doThrow(new RuntimeException("登录过期")).when(userService).updateMe(any(UpdateMeRequest.class));

        mockMvc.perform(put("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"13413441344\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("登录过期"));
    }

    @Test
    void changePasswordSuccess() throws Exception
    {
        mockMvc.perform(put("/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "oldPassword": "oldpass123",
                                  "newPassword": "newpass123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        verify(userService).changePassword(any(ChangePasswordRequest.class));
    }

    @Test
    void changePasswordWithWrongOldPwd() throws Exception
    {
        doThrow(new RuntimeException("密码错误")).when(userService).changePassword(any(ChangePasswordRequest.class));

        mockMvc.perform(put("/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "oldPassword": "oldpass123",
                                  "newPassword": "newpass123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("密码错误"));
    }

    @Test
    void changePasswordRejectsNullOldPassword() throws Exception
    {
        mockMvc.perform(put("/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "newPassword": "newpass123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("旧密码不能为空"));
    }

    @Test
    void changePasswordRejectsShortOldPassword() throws Exception
    {
        mockMvc.perform(put("/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "oldPassword": "short",
                                  "newPassword": "newpass123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("密码长度不低于8位"));
    }

    @Test
    void changePasswordRejectsNullNewPassword() throws Exception
    {
        mockMvc.perform(put("/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "oldPassword": "oldpass123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("新密码不能为空"));
    }

    @Test
    void changePasswordRejectsShortNewPassword() throws Exception
    {
        mockMvc.perform(put("/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "oldPassword": "oldpass123",
                                  "newPassword": "short"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("密码长度不低于8位"));
    }

    @Test
    void changePasswordNotExpire() throws Exception
    {
        doThrow(new RuntimeException("登录过期")).when(userService).changePassword(any(ChangePasswordRequest.class));

        mockMvc.perform(put("/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "oldPassword": "oldpass123",
                                  "newPassword": "newpass123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("登录过期"));
    }

    @Test
    void changePasswordUserNotExist() throws Exception
    {
        doThrow(new UserNotExistException("用户不存在")).when(userService).changePassword(any(ChangePasswordRequest.class));

        mockMvc.perform(put("/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "oldPassword": "oldpass123",
                                  "newPassword": "newpass123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("用户不存在"));
    }

    @Test
    void uploadAvatarSuccess() throws Exception
    {
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.png", MediaType.IMAGE_PNG_VALUE, "image-content".getBytes());
        LocalDateTime createTime = LocalDateTime.of(2026, 7, 18, 15, 0);
        FileResponse response = new FileResponse(
                null, "https://example.com/avatar.png", "avatar.png",
                MediaType.IMAGE_PNG_VALUE, file.getSize(), 1, createTime);
        when(userService.upLoadAvatar(any())).thenReturn(response);

        mockMvc.perform(multipart("/files")
                        .file(file)
                        .param("bizType", "avatar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.fileId").isEmpty())
                .andExpect(jsonPath("$.data.url").value("https://example.com/avatar.png"))
                .andExpect(jsonPath("$.data.fileName").value("avatar.png"))
                .andExpect(jsonPath("$.data.contentType").value(MediaType.IMAGE_PNG_VALUE))
                .andExpect(jsonPath("$.data.size").value(file.getSize()))
                .andExpect(jsonPath("$.data.uploadedBy").value(1));

        verify(userService).upLoadAvatar(any());
    }

    @Test
    void uploadAvatarDefaultsBizTypeWhenMissing() throws Exception
    {
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.jpg", MediaType.IMAGE_JPEG_VALUE, "image-content".getBytes());
        when(userService.upLoadAvatar(any())).thenReturn(new FileResponse(
                null, "https://example.com/avatar.jpg", "avatar.jpg",
                MediaType.IMAGE_JPEG_VALUE, file.getSize(), 1, LocalDateTime.now()));

        mockMvc.perform(multipart("/files").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.url").value("https://example.com/avatar.jpg"));

        verify(userService).upLoadAvatar(any());
    }

    @Test
    void uploadAvatarRejectsUnsupportedBizType() throws Exception
    {
        MockMultipartFile file = new MockMultipartFile(
                "file", "dish.png", MediaType.IMAGE_PNG_VALUE, "image-content".getBytes());

        mockMvc.perform(multipart("/files")
                        .file(file)
                        .param("bizType", "dish-image"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("当前只支持头像上传"));

        verify(userService, never()).upLoadAvatar(any());
    }

    private MeResponse createResponse()
    {
        MeResponse response = new MeResponse();
        response.setId(1);
        response.setUsername("alice01");
        response.setNickname("Alice");
        response.setPhone("13800000000");
        response.setStatus("ACTIVE");
        response.setAvatarFileId(1);
        response.setAvatarUrl("http://example.com/avatar.jpg");
        response.setRole(UserRole.USER);
        response.setCreateTime(LocalDateTime.of(2024, 6, 1, 12, 0));
        response.setUpdateTime(LocalDateTime.of(2024, 6, 1, 12, 0));
        return response;
    }
}
