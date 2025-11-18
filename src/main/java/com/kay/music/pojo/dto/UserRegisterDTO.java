package com.kay.music.pojo.dto;

import com.baomidou.mybatisplus.annotation.TableField;
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
 * @date 2025/11/18 23:39
 */
@Data
@Schema(name = "UserRegisterDTO", description = "用户注册类DTO实体")
public class UserRegisterDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = MessageConstant.USERNAME + MessageConstant.NOT_NULL)
    // 用户名必须是 3~16 位，只能由 字母、数字、下划线、连字符 组成。
    @Pattern(regexp = "^[a-zA-Z0-9_-]{3,16}$", message = MessageConstant.USERNAME + MessageConstant.FORMAT_ERROR)
    @Schema(description = "用户名（3-16位 字母/数字/下划线/连字符）", example = "kay")
    private String username;

    @NotBlank(message = MessageConstant.PASSWORD + MessageConstant.NOT_NULL)
    // 密码格式：3~16 位，只能由 字母、数字、下划线、连字符 组成。
    @Pattern(regexp = "^[a-zA-Z0-9_-]{3,18}$", message = MessageConstant.PASSWORD + MessageConstant.FORMAT_ERROR)
    @Schema(description = "用户密码（3-18位 字母/数字/下划线/连字符）", example = "123456")
    private String password;

    @NotBlank(message = MessageConstant.EMAIL + MessageConstant.NOT_NULL)
    @Email(message = MessageConstant.EMAIL + MessageConstant.FORMAT_ERROR)
    @Schema(description = "用户邮箱" , example = "test@qq.com")
    private String email;

    @NotBlank(message = MessageConstant.VERIFICATION_CODE + MessageConstant.NOT_NULL)
    @Pattern(regexp = "^[0-9a-zA-Z]{6}$", message = MessageConstant.VERIFICATION_CODE + MessageConstant.FORMAT_ERROR)
    @Schema(description = "验证码，验证码格式：6位字符（大小写字母、数字）" , example = "123456")
    private String verificationCode;

}
