package org.example.stall_manage.exception;

import lombok.Getter;

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
