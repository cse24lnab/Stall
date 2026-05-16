package org.lab.stall_manage.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.Mergeable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse{
       private String accessToken;
       private String tokenType;
       private Integer expiresIn;
       private MeResponse user;
}
