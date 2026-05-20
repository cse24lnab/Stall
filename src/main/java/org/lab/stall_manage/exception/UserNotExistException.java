package org.lab.stall_manage.exception;

import lombok.Getter;
import org.lab.stall_manage.pojo.User;

@Getter
public class UserNotExistException extends RuntimeException{
    public UserNotExistException(String message)
    {
        super(message);
    }
}
