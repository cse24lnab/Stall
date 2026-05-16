package org.lab.stall_manage.pojo;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stall {

    private Integer id;

    @NotBlank(message = "摊位名字不能为空")
    private String name;

    //状态: 0=休息, 1=营业 默认0
    private Integer currentStatus;
    private String noonLocation;
    private String eveningLocation;
    private LocalTime noonStartTime;
    private LocalTime eveningStartTime;
    private LocalTime noonEndTime;
    private LocalTime eveningEndTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    //逻辑删除
    private Integer isDelete;

    //用户身份
    private Integer ownerUserId;
    //todo 图片url
}
