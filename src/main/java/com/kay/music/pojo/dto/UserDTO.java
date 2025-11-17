package com.kay.music.pojo.dto;

import com.kay.music.constant.MessageConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Kay
 * @date 2025/11/17 17:57
 */
@Data
@Schema(name = "UserDTO", description = "用户类DTO实体")
public class UserDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @NotBlank(message = MessageConstant.USERNAME + MessageConstant.NOT_NULL)
    // 用户名必须是 3~16 位，只能由 字母、数字、下划线、连字符 组成。
    @Pattern(regexp = "^[a-zA-Z0-9_-]{3,16}$", message = MessageConstant.USERNAME + MessageConstant.FORMAT_ERROR)
    @Schema(description = "用户名（3-16位 字母/数字/下划线/连字符）", example = "kay")
    private String username;


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

}
