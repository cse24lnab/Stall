package org.lab.stall_manage.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.lab.stall_manage.pojo.enums.UserRole;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MeResponse {
    private Integer id;
    private String username;
    private String nickname;
    private String phone;
    private Integer avatarFileId;
    private String avatarUrl;
    private UserRole role;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
