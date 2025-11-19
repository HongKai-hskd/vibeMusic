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
 * @date 2025/11/19 21:48
 */
@Data
@Schema(name = "UserResetPasswordDTO", description = "用户重置密码DTO实体")
public class UserResetPasswordDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = MessageConstant.EMAIL + MessageConstant.NOT_NULL)
    @Email(message = MessageConstant.EMAIL + MessageConstant.FORMAT_ERROR)
    @Schema(description = "用户邮箱" , example = "test@qq.com")
    private String email;

    @NotBlank(message = MessageConstant.VERIFICATION_CODE + MessageConstant.NOT_NULL)
    @Pattern(regexp = "^[0-9a-zA-Z]{6}$", message = MessageConstant.VERIFICATION_CODE + MessageConstant.FORMAT_ERROR)
    @Schema(description = "验证码，验证码格式：6位字符（大小写字母、数字）" , example = "123456")
    private String verificationCode;

    @NotBlank(message = MessageConstant.PASSWORD + MessageConstant.NOT_NULL)
    @Pattern(regexp = "^[a-zA-Z0-9_-]{3,18}$", message = MessageConstant.PASSWORD + MessageConstant.FORMAT_ERROR)
    @Schema(description = "新密码（3-18位 字母/数字/下划线/连字符）", example = "123456")
    private String newPassword;

    @NotBlank(message = MessageConstant.PASSWORD + MessageConstant.NOT_NULL)
    @Pattern(regexp = "^[a-zA-Z0-9_-]{3,18}$", message = MessageConstant.PASSWORD + MessageConstant.FORMAT_ERROR)
    @Schema(description = "确认密码（3-18位 字母/数字/下划线/连字符）", example = "123456")
    private String repeatPassword;

}
