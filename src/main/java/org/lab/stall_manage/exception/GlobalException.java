package org.lab.stall_manage.exception;

import lombok.extern.slf4j.Slf4j;
import org.lab.stall_manage.pojo.Result;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.StringUtils;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RestControllerAdvice
public class GlobalException {
    private static final Pattern DUPLICATE_ENTRY_PATTERN =
            Pattern.compile("Duplicate entry '(.+?)' for key");

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> hdleMethArgNotValidEx(MethodArgumentNotValidException ex)
    {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String msg=(fieldError!=null)?fieldError.getDefaultMessage():"参数校验失败";
        log.error("Request body validation failed", ex);
        return Result.error(msg);
    }

    /**
     * @Valitated注解处理非类参数，抛出该异常，输入不满足要求
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public Result<?> hdlHandlerMethodValidationEx(HandlerMethodValidationException ex)
    {
        String msg = "参数校验失败";
        List<MessageSourceResolvable> errors = ex.getParameterValidationResults().stream()
                .findFirst()
                .map(result -> result.getResolvableErrors())
                .orElse(List.of());

        if (!errors.isEmpty() && StringUtils.hasText(errors.get(0).getDefaultMessage())) {
            msg = errors.get(0).getDefaultMessage();
        }

        log.error("Handler method validation failed", ex);
        return Result.error(msg);
    }

    /**
     * @Valid注解处理 类参数,类中属性不满足输入要求抛出该异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<?> hdlMethodArgTypeMismatchEx(MethodArgumentTypeMismatchException ex)
    {
        String msg = StringUtils.hasText(ex.getName())
                ? "参数 " + ex.getName() + " 类型错误"
                : "参数类型错误";
        log.error("Method argument type mismatch", ex);
        return Result.error(msg);
    }

    /**
     * 参数数目不匹配
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<?> hdlMissingServletRequestParameterEx(MissingServletRequestParameterException ex)
    {
        String msg = "缺少必要参数: " + ex.getParameterName();
        log.error("Missing servlet request parameter", ex);
        return Result.error(msg);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<?> hdlHttpMessageNotReadableEx(HttpMessageNotReadableException ex)
    {
        log.error("Request body not readable", ex);
        return Result.error("请求体格式错误");
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public Result<?> hdlDupliKeyEx(DuplicateKeyException ex)
    {
        log.error("Duplicate key exception", ex);
        String msg = ex.getMessage();
        if (!StringUtils.hasText(msg)) {
            return Result.error("数据已存在");
        }

        Matcher matcher = DUPLICATE_ENTRY_PATTERN.matcher(msg);
        if (matcher.find() && StringUtils.hasText(matcher.group(1))) {
            return Result.error(matcher.group(1) + "已存在");
        }
        return Result.error("数据已存在");
    }

    /**
     * 自定义异常类
     * @see StallNotExistException
     */
    @ExceptionHandler(StallNotExistException.class)
    public Result<?> hdlStallNExistEx(StallNotExistException ex)
    {
        log.error("Stall not found", ex);
        return Result.error(ex.getMessage());
    }

    /**
     * 防御性编程
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<?> hdlIllegalArgEx(IllegalArgumentException ex) {
        log.error("Illegal argument", ex);
        return Result.error(ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<?> hdleRuntimeEx(RuntimeException ex)
    {
        String msg=ex.getMessage();
        log.error("Runtime exception", ex);
        if(!StringUtils.hasText(msg))
        {
            return Result.error("操作失败");
        }
        return Result.error(msg);
    }

    @ExceptionHandler(Exception.class)
    public Result<?> hdleEx(Exception ex)
    {
        log.error("Unhandled exception", ex);
        return Result.error("服务器出错，稍后再试");
    }
}
