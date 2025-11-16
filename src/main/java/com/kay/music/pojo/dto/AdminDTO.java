package com.kay.music.pojo.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.kay.music.constant.MessageConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Kay
 * @date 2025/11/16 12:47
 */
@Data
@Schema(name = "AdminDTO", description = "AdminDTO")
public class AdminDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @NotBlank(message = MessageConstant.USERNAME + MessageConstant.NOT_NULL)
    // 用户名必须是 3~16 位，只能由 字母、数字、下划线、连字符 组成。
    @Pattern(regexp = "^[a-zA-Z0-9_-]{3,16}$", message = MessageConstant.USERNAME + MessageConstant.FORMAT_ERROR)
    @Schema(description = "管理员用户名（3-16位 字母/数字/下划线/连字符）", example = "admin")
    private String username;


    // 密码不能为空、不能全是空格
    @NotBlank(message = MessageConstant.PASSWORD + MessageConstant.NOT_NULL)
    // 密码格式：3-18 位数字、字母、符号的任意两种组合
    @Pattern(regexp = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z\\W]{3,18}$", message = MessageConstant.PASSWORD + MessageConstant.FORMAT_ERROR)
    @Schema(description = "管理员密码（3-18位，两种字符类型组合）", example = "123456")
    private String password;
}
