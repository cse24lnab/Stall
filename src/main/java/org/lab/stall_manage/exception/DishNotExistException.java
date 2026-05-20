package org.lab.stall_manage.exception;

import lombok.Getter;

@Getter
public class DishNotExistException extends RuntimeException{
    private final Integer code;

    public DishNotExistException(String msg)
    {
        super(msg);
        code=500;
    }

    public DishNotExistException(Integer code, String msg)
    {
        super(msg);
        this.code=code;
    }
}
