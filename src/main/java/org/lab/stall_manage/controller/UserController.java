package org.lab.stall_manage.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.lab.stall_manage.dto.ChangePasswordRequest;
import org.lab.stall_manage.dto.UpdateMeRequest;
import org.lab.stall_manage.pojo.Result;
import org.lab.stall_manage.service.UserService;
import org.lab.stall_manage.vo.MeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/users/me")
    public Result<MeResponse> findMe()
    {
        return Result.success(userService.findMe().orElse(null));
    }

    @PutMapping("/users/me")
    public Result<?> updateMe(@RequestBody @Valid UpdateMeRequest updateMeRequest)
    {
        userService.updateMe(updateMeRequest);
        return Result.success();
    }

    @PutMapping("/users/me/password")
    public Result<?> changePassword(@RequestBody @Valid ChangePasswordRequest changePasswordRequest)
    {
        userService.changePassword(changePasswordRequest);
        return Result.success();
    }
}
