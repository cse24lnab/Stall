package org.lab.stall_manage.pojo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dish {
    private Integer id;

    @NotNull(message ="摊位id不能为空")
    private Integer stallId;

    @NotBlank(message = "菜品名字不能为空")
    //notblank就是不为null也不为空串
    private String name;

    @NotNull(message = "价格不能为空")
    @Min(value = 0,message = "价格不能小于0")
    private BigDecimal price;

    //状态: 0=有货, 1=售罄 默认0
    private Integer isSoldOut;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    //逻辑删除
    private Integer isDelete;
}
