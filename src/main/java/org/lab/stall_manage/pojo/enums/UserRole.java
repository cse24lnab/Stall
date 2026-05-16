package org.lab.stall_manage.pojo.enums;

import lombok.Getter;

@Getter
public enum UserRole {
    USER(0,"USER"),
    MERCHANT(1,"MERCHANT"),
    ADMIN(2,"ADMIN");

    private final int code;
    private final String role;

    UserRole(int code,String role){
        this.code=code;
        this.role=role;
    };
}
