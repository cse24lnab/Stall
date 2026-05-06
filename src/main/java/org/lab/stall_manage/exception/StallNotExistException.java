package org.lab.stall_manage.exception;

import lombok.Getter;

//用于解决全局异常处理器捕获异常顺序
@Getter
public class StallNotExistException extends RuntimeException{
    private final Integer code;

    public StallNotExistException(String message)
    {
        super(message);
        this.code=500;
    }

    public StallNotExistException(Integer code,String msg)
    {
        super(msg);
        this.code=code;
    }
}
