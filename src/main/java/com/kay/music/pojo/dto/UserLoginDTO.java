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
 * @date 2025/11/19 11:26
 */
@Data
@Schema(name = "UserLoginDTO", description = "用户登录DTO类实体")
public class UserLoginDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = MessageConstant.EMAIL + MessageConstant.NOT_NULL)
    @Email(message = MessageConstant.EMAIL + MessageConstant.FORMAT_ERROR)
    @Schema(description = "用户邮箱" , example = "2500357364@qq.com")
    private String email;

    @NotBlank(message = MessageConstant.PASSWORD + MessageConstant.NOT_NULL)
    // 密码格式：3~16 位，只能由 字母、数字、下划线、连字符 组成。
    @Pattern(regexp = "^[a-zA-Z0-9_-]{3,18}$", message = MessageConstant.PASSWORD + MessageConstant.FORMAT_ERROR)
    @Schema(description = "用户密码（3-18位 字母/数字/下划线/连字符）", example = "123456")
    private String password;

}
