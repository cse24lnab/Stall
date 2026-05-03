package org.example.stall_manage.exception;

import lombok.extern.slf4j.Slf4j;
import org.example.stall_manage.pojo.Result;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.util.StringUtils;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLSyntaxErrorException;

@Slf4j
@RestControllerAdvice
public class GlobalException {
    @ExceptionHandler(RuntimeException.class)
    public Result<?> hdleRuntimeEx(RuntimeException ex)
    {
        String msg=ex.getMessage();
        log.error(msg);
        if(!StringUtils.hasText(msg))
        {
            return Result.error("操作失败");
        }
        return Result.error(msg);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> hdleMethArgNotValidEx(MethodArgumentNotValidException ex)
    {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String msg=(fieldError!=null)?fieldError.getDefaultMessage():"参数校验失败";
        log.error(ex.getMessage());
        return Result.error(msg);
    }

    @ExceptionHandler(Exception.class)
    public Result<?> hdleEx(Exception ex)
    {
        log.error(ex.getMessage());
        return Result.error("服务器出错，稍后再试");
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public Result<?> hdlDupliKeyEx(DuplicateKeyException ex)
    {
        String msg=ex.getMessage();
        log.error(msg);
        int it=msg.indexOf("Duplicate entry");
        msg=msg.substring(it);
        String[] err=msg.split(" ");
        return Result.error(err[2]+"已存在");
    }

    @ExceptionHandler(StallNotExistException.class)
    public Result<?> hdlStallNExistEx(StallNotExistException ex)
    {
        String msg=ex.getMessage();
        log.error(msg);
        return Result.error(msg);
    }

    //todo捕获sql语法错误异常
}
