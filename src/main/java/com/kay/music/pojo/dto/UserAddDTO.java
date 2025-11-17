package com.kay.music.pojo.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.kay.music.constant.MessageConstant;
import com.kay.music.enumeration.UserStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Kay
 * @date 2025/11/17 19:32
 */
@Data
@Schema(name = "UserAddDTO", description = "新增用户DTO实体")
public class UserAddDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = MessageConstant.USERNAME + MessageConstant.NOT_NULL)
    @Pattern(regexp = "^[a-zA-Z0-9_-]{3,16}$", message = MessageConstant.USERNAME + MessageConstant.FORMAT_ERROR)
    @Schema(description = "用户名（3-16位 字母/数字/下划线/连字符）", example = "kay")
    private String username;

    @NotBlank(message = MessageConstant.PASSWORD + MessageConstant.NOT_NULL)
    // 密码格式：3~16 位，只能由 字母、数字、下划线、连字符 组成。
    @Pattern(regexp = "^[a-zA-Z0-9_-]{3,18}$", message = MessageConstant.PASSWORD + MessageConstant.FORMAT_ERROR)
    @Schema(description = "用户密码（3-18位 字母/数字/下划线/连字符）", example = "123456")
    private String password;

    @Pattern(regexp = "^1[3456789]\\d{9}$", message = MessageConstant.PHONE + MessageConstant.FORMAT_ERROR)
    @Schema(description = "手机号格式：1开头，11位数字", example = "15970001234")
    private String phone;

    @NotBlank(message = MessageConstant.EMAIL + MessageConstant.NOT_NULL)
    @Email(message = MessageConstant.EMAIL + MessageConstant.FORMAT_ERROR)
    @Schema(description = "用户邮箱" , example = "test@qq.com")
    private String email;

    @Pattern(regexp = "^.{0,100}$", message = MessageConstant.WORD_LIMIT_ERROR)
    @Schema(description = "用户简介格式：100 字以内")
    private String introduction;

    @Schema(description = "用户状态：0-启用，1-禁用" , example = "0")
    private UserStatusEnum userStatus;

}

