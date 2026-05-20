package org.lab.stall_manage.pojo.enums;

import lombok.Getter;

//todo 自定义code映射
@Getter
public enum Status {
    DISABLE(0,"未激活"),
    ACTIVE(1,"激活");

    private Integer id;
    private String status;

    Status(int id, String status) {
        this.id=id;
        this.status=status;
    }


}
