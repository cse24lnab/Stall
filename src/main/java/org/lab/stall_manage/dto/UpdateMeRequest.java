package org.lab.stall_manage.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateMeRequest {
    private String nickname;
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式错误")
    private String phone;
    private Integer avatarFileId;
}
