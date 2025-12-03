package com.kay.music.result;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.kay.music.constant.MessageConstant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: Kay
 * @date:   2025/11/16 12:43
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Result<T> {
    private Integer code;   // HTTP 状态码  200-成功  400-客户端错误  500-服务器错误
    private String message; // 提示信息

    // 当值为 null 时，写入空数组
    @JsonSerialize(nullsUsing = NullToEmptyArraySerializer.class)
    private T data;         // 响应数据

    // 快速返回操作成功响应结果(默认提示信息)
    public static <T> Result<T> success(T data) {
        return new Result<>(200, MessageConstant.OPERATION + MessageConstant.SUCCESS, data);
    }

    // 快速返回操作成功响应结果(默认提示信息)
    public static Result success() {
        return new Result(200, MessageConstant.OPERATION + MessageConstant.SUCCESS, null);
    }

    // 快速返回操作失败响应结果(默认提示信息 - 400 客户端错误)
    public static Result error() {
        return new Result(400, MessageConstant.OPERATION + MessageConstant.FAILED, null);
    }

    // 快速返回操作成功响应结果(带响应数据和自定义提示信息)
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    // 快速返回操作成功响应结果(带自定义提示信息)
    public static Result success(String message) {
        return new Result(200, message, null);
    }

    // 快速返回操作失败响应结果(带自定义提示信息 - 400 客户端错误)
    public static Result error(String message) {
        return new Result(400, message, null);
    }

    // 快速返回 404 未找到错误
    public static Result notFound(String message) {
        return new Result(404, message, null);
    }

    // 快速返回 500 服务器错误
    public static Result serverError(String message) {
        return new Result(500, message, null);
    }

}
