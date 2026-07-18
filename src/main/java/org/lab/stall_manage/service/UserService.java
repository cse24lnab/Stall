package org.lab.stall_manage.service;


import org.lab.stall_manage.dto.ChangePasswordRequest;
import org.lab.stall_manage.dto.UpdateMeRequest;
import org.lab.stall_manage.vo.FileResponse;
import org.lab.stall_manage.vo.MeResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface UserService {

    /**
     * 更新个人信息
     * @throws IllegalArgumentException 参数不能为空
     * @throws RuntimeException 登录过期
     */
    void updateMe(UpdateMeRequest updateMeRequest);

    /**
     * 修改密码
     * @throws IllegalArgumentException 参数不能为空
     * @throws org.lab.stall_manage.exception.UserNotExistException 用户不存在
     * @throws RuntimeException 密码错误 登录过期
     */
    void changePassword(ChangePasswordRequest changePasswordRequest);

    /**
     * 上传头像
     * @throws com.aliyun.oss.OSSException 文件上传失败
     * @throws com.aliyun.oss.ClientException 文件上传失败
     * @throws RuntimeException 登录过期
     * @throws IllegalArgumentException 文件不能为空，文件大小不能超过2MB
     */
    FileResponse upLoadAvatar(MultipartFile file);

    /**
     * 用户查看自身信息
     * @throws RuntimeException 登录过期
     */
    Optional<MeResponse> findMe();
}
