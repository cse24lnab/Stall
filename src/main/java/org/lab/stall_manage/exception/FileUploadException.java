package org.lab.stall_manage.exception;

import lombok.Getter;

@Getter
public class FileUploadException extends RuntimeException{
    private final Integer code;
    public FileUploadException(String msg)
    {
        super(msg);
        code=500;
    }
    public FileUploadException(String msg,Integer code)
    {
        super(msg);
        this.code=code;
    }
}
