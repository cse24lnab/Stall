package org.lab.stall_manage.pojo.enums;

import lombok.Getter;

//todo 自定义code映射
@Getter
public enum UserRole {
    USER(0,"普通用户"),
    MERCHANT(1,"商家"),
    ADMIN(2,"管理员");

    private final int code;
    private final String role;

    UserRole(int code,String role){
        this.code=code;
        this.role=role;
    };

}
