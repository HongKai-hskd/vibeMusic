package com.kay.music.handler;

import com.kay.music.constant.MessageConstant;
import com.kay.music.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Kay
 * @date 2025/11/16 12:58
 */
@Slf4j
@RestControllerAdvice
/*@RestControllerAdvice = @ControllerAdvice	用于全局增强 Controller（例如统一异常处理、数据绑定）
                        + @ResponseBody	让返回值自动序列化为 JSON*/
public class GlobalExceptionHandler {

    private static final Pattern DUPLICATE_ENTRY_PATTERN = Pattern.compile("Duplicate entry '(.+?)' for key '(.+?)'");

    /**
     * @Description: 处理 @Valid 参数校验异常
     * @Author: Kay
     * @date:   2025/11/16 13:03
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 返回 400 状态码 , 能让前端明确知道是 400 错误（输入参数错误）
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleValidationException(MethodArgumentNotValidException ex) {
        log.error("参数校验异常：{}", ex.getMessage(), ex);
        /*StringBuilder errorMessage = new StringBuilder("输入参数校验失败: ");

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errorMessage.append(error.getDefaultMessage()).append("; ");
        }*/

        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return Result.error(errorMessage.toString());
    }



    /**
     * @Description:  处理SQL异常
     * @Author: Kay
     * @date:   2025/11/21 10:58
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result handleSqlIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException ex) {
        log.error("SQL异常：{}", ex.getMessage(), ex);
        Matcher matcher = DUPLICATE_ENTRY_PATTERN.matcher(ex.getMessage());
        try {
            if (matcher.find()) {
                String msg = matcher.group(2) + " " + MessageConstant.ALREADY_EXISTS;
                return Result.error(msg);
            }
        } catch (IndexOutOfBoundsException e) {
            log.error("解析SQL异常时发生错误：{}", e.getMessage(), e);
        }
        return Result.error(MessageConstant.UNKNOWN_ERROR);
    }
}
