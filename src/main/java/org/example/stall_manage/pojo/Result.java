package org.example.stall_manage.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private Integer code;
    private String msg;
    private T data;

    public static <E> Result<E> success(E data)
    {
        return new Result<>(1,"success",data);
    }

    public static <E> Result<E> success()
    {
        return new Result<>(1,"success",null);
    }

    public static <E> Result<E> error(String msg)
    {
        return new Result<>(0,msg,null);
    }
}
