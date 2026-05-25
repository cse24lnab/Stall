package org.lab.stall_manage.service;


import org.lab.stall_manage.dto.ChangePasswordRequest;
import org.lab.stall_manage.dto.UpdateMeRequest;
import org.lab.stall_manage.vo.MeResponse;

import java.util.Optional;

public interface UserService {

    /**
     * 更新个人信息
     */
    public void updateMe(UpdateMeRequest updateMeRequest);

    /**
     * 修改密码
     */
    public void changePassword(ChangePasswordRequest changePasswordRequest);

    /**
     * 用户查看自身信息
     */
    public Optional<MeResponse> findMe();
}
