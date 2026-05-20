package org.lab.stall_manage.pojo;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.lab.stall_manage.pojo.enums.Status;
import org.lab.stall_manage.pojo.enums.UserRole;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private Integer id;

    @NotBlank(message = "用户名不能为空")
    private String username;
    private String passwordHash;
    private String nickname;
    private String phone;
    private Integer avatarFileId;
    private String avatarUrl;
    private UserRole role;
    private Status status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDelete;
}
