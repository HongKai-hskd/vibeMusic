package com.kay.music.handler;

import com.kay.music.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author Kay
 * @date 2025/11/16 12:58
 */
@Slf4j
@RestControllerAdvice
/*@RestControllerAdvice = @ControllerAdvice	用于全局增强 Controller（例如统一异常处理、数据绑定）
                        + @ResponseBody	让返回值自动序列化为 JSON*/
public class GlobalExceptionHandler {

    /**
     * @Description: 处理 @Valid 参数校验异常
     * @Author: Kay
     * @date:   2025/11/16 13:03
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleValidationException(MethodArgumentNotValidException ex) {

        StringBuilder errorMessage = new StringBuilder("输入参数校验失败: ");

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errorMessage.append(error.getDefaultMessage()).append("; ");
        }

        return Result.error(errorMessage.toString());
    }
}
