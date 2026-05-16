package org.lab.stall_manage.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.lab.stall_manage.pojo.enums.UserRole;

//解耦 上下文所需属性
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrentUser {
    private Integer id;
    private String username;
    private UserRole role;
}
