package org.lab.stall_manage.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.lab.stall_manage.dto.ChangePasswordRequest;
import org.lab.stall_manage.dto.UpdateMeRequest;
import org.lab.stall_manage.pojo.Result;
import org.lab.stall_manage.service.UserService;
import org.lab.stall_manage.vo.FileResponse;
import org.lab.stall_manage.vo.MeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping(value = "/files",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<?> upload(@RequestParam("file") MultipartFile file,@RequestParam(value = "bizType",required = false)String bizType)
    {
        String actualBizType= StringUtils.hasText(bizType)?bizType:"avatar";
        if(!actualBizType.equals("avatar"))
        {
            throw new IllegalArgumentException("当前只支持头像上传");
        }
        FileResponse fileResponse = userService.upLoadAvatar(file);
        return Result.success(fileResponse);
    }
}
