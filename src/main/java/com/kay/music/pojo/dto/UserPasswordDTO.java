package com.kay.music.pojo.dto;

import com.kay.music.constant.MessageConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Kay
 * @date 2025/11/19 21:37
 */
@Data
@Schema(name = "UserPasswordDTO", description = "用户修改密码DTO实体")
public class UserPasswordDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = MessageConstant.PASSWORD + MessageConstant.NOT_NULL)
    @Pattern(regexp = "^[a-zA-Z0-9_-]{3,18}$", message = MessageConstant.PASSWORD + MessageConstant.FORMAT_ERROR)
    @Schema(description = "旧密码（3-18位 字母/数字/下划线/连字符）", example = "123456")
    private String oldPassword;

    @NotBlank(message = MessageConstant.PASSWORD + MessageConstant.NOT_NULL)
    @Pattern(regexp = "^[a-zA-Z0-9_-]{3,18}$", message = MessageConstant.PASSWORD + MessageConstant.FORMAT_ERROR)
    @Schema(description = "新密码（3-18位 字母/数字/下划线/连字符）", example = "123456")
    private String newPassword;

    @NotBlank(message = MessageConstant.PASSWORD + MessageConstant.NOT_NULL)
    @Pattern(regexp = "^[a-zA-Z0-9_-]{3,18}$", message = MessageConstant.PASSWORD + MessageConstant.FORMAT_ERROR)
    @Schema(description = "确认密码（3-18位 字母/数字/下划线/连字符）", example = "123456")
    private String repeatPassword;
}
