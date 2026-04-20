package org.example.stall_manage.exception;

import org.example.stall_manage.pojo.Result;
import org.springframework.util.StringUtils;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalException {
    @ExceptionHandler(RuntimeException.class)
    public Result<?> hdleRuntimeEx(RuntimeException ex)
    {
        String msg=ex.getMessage();
        if(!StringUtils.hasText(msg))
        {
            return Result.error("操作失败");
        }
        System.out.println("出错"+msg);
        return Result.error(msg);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> hdleMethArgNotValidEx(MethodArgumentNotValidException ex)
    {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String msg=(fieldError!=null)?fieldError.getDefaultMessage():"参数校验失败";
        System.out.println("出错"+msg);
        return Result.error(msg);
    }

    @ExceptionHandler(Exception.class)
    public Result<?> hdleEx(Exception ex)
    {
        ex.printStackTrace();
        return Result.error("服务器出错，稍后再试");
    }
}
