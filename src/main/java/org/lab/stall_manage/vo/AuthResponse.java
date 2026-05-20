package org.lab.stall_manage.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private Integer id;
    private String username;
    private String nickname;
    private String phone;
    private String status;
}
