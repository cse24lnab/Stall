package org.lab.stall_manage.exception;

import lombok.Getter;

@Getter
public class DishNotExitException extends RuntimeException{
    private final Integer code;

    public DishNotExitException(String msg)
    {
        super(msg);
        code=500;
    }

    public DishNotExitException(Integer code, String msg)
    {
        super(msg);
        this.code=code;
    }
}
