package org.lab.stall_manage.service.impl;

import org.lab.stall_manage.context.BaseContext;
import org.lab.stall_manage.context.CurrentUser;
import org.lab.stall_manage.dto.ChangePasswordRequest;
import org.lab.stall_manage.dto.UpdateMeRequest;
import org.lab.stall_manage.exception.UserNotExistException;
import org.lab.stall_manage.mapper.UserMapper;
import org.lab.stall_manage.pojo.User;
import org.lab.stall_manage.service.UserService;
import org.lab.stall_manage.utils.AliyunOssUtil;
import org.lab.stall_manage.vo.MeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AliyunOssUtil aliyunOssUtil;

    @Override
    public Optional<MeResponse> findMe() {
        Integer id=getCurrentId();
        User user = userMapper.find(id);
        if(user == null)
        {
            return Optional.empty();
        }
        MeResponse meResponse=new MeResponse(
                user.getId(),user.getUsername(),user.getNickname(),user.getPhone(),
                user.getAvatarFileId(),user.getAvatarUrl(),user.getRole(),user.getStatus().name(),
                user.getCreateTime(),user.getUpdateTime());
        return Optional.of(meResponse);
    }

    @Override
    public void updateMe(UpdateMeRequest updateMeRequest) {
        if(updateMeRequest == null || !hasValue(updateMeRequest))
        {
            throw new IllegalArgumentException("参数不能为空");
        }
        Integer id=getCurrentId();
        User user=new User();
        user.setId(id);
        user.setNickname(updateMeRequest.getNickname());
        user.setPhone(updateMeRequest.getPhone());
        user.setAvatarFileId(updateMeRequest.getAvatarFileId());
        userMapper.update(user);
    }

    @Override
    public void changePassword(ChangePasswordRequest changePasswordRequest) {
        //不再校验密码长度
        if(isBlank(changePasswordRequest))
        {
            throw new IllegalArgumentException("参数不能为空");
        }
        Integer id=getCurrentId();
        User user = userMapper.find(id);
        if(user == null)
        {
            throw new UserNotExistException("用户不存在");
        }
        if (!StringUtils.hasText(user.getPasswordHash())) {
            throw new RuntimeException("用户数据异常，无法修改密码");
        }
        boolean matches = passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPasswordHash());
        if(!matches)
        {
            throw new RuntimeException("密码错误");
        }
        String newPassword=passwordEncoder.encode(changePasswordRequest.getNewPassword());
        User newUser=new User();
        newUser.setId(id);
        newUser.setPasswordHash(newPassword);
        userMapper.update(newUser);
    }

    @Override
    public void upLoadAvatar(MultipartFile file) {
        //defend
        if(file == null || file.isEmpty())
        {
            throw new IllegalArgumentException("文件不能为空");
        }
        try {
            String uploadUrl = aliyunOssUtil.upload(file);
            Integer id=getCurrentId();
            User user=new User();
            user.setId(id);
            user.setAvatarUrl(uploadUrl);
            userMapper.update(user);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Integer getCurrentId()
    {
        CurrentUser currentUser = BaseContext.getCurrentUser();
        //防御性编程
        if(currentUser == null || currentUser.getId() == null)
        {
            throw new RuntimeException("登录过期");
        }
        return currentUser.getId();
    }

    //有一个有值就行
    private boolean hasValue(UpdateMeRequest updateMeRequest)
    {
        return StringUtils.hasText(updateMeRequest.getNickname()) ||
                StringUtils.hasText(updateMeRequest.getPhone()) ||
                updateMeRequest.getAvatarFileId() != null;
    }

    private boolean isBlank(ChangePasswordRequest changePasswordRequest)
    {
        return changePasswordRequest == null || !StringUtils.hasText(changePasswordRequest.getOldPassword())
                || !StringUtils.hasText(changePasswordRequest.getNewPassword());
    }

}
