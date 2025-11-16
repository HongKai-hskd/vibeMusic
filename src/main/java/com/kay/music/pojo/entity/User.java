package com.kay.music.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.kay.music.constant.MessageConstant;
import com.kay.music.enumeration.UserStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author Kay
 * @date 2025/11/16 23:54
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_user")
@Schema(name = "User", description = "用户类实体")
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id" , type = IdType.AUTO)
    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @NotBlank(message = MessageConstant.USERNAME + MessageConstant.NOT_NULL)
    // 用户名必须是 3~16 位，只能由 字母、数字、下划线、连字符 组成。
    @Pattern(regexp = "^[a-zA-Z0-9_-]{3,16}$", message = MessageConstant.USERNAME + MessageConstant.FORMAT_ERROR)
    @Schema(description = "用户名（3-16位 字母/数字/下划线/连字符）", example = "kay")
    @TableField("username")
    private String username;

    @TableField("password")
    @NotBlank(message = MessageConstant.PASSWORD + MessageConstant.NOT_NULL)
    // 密码格式：3~16 位，只能由 字母、数字、下划线、连字符 组成。
    @Pattern(regexp = "^[a-zA-Z0-9_-]{3,18}$", message = MessageConstant.PASSWORD + MessageConstant.FORMAT_ERROR)
    @Schema(description = "用户密码（3-18位 字母/数字/下划线/连字符）", example = "123456")
    private String password;

    @Pattern(regexp = "^1[3456789]\\d{9}$", message = MessageConstant.PHONE + MessageConstant.FORMAT_ERROR)
    @TableField("phone")
    @Schema(description = "手机号格式：1开头，11位数字", example = "15970001234")
    private String phone;

    @NotBlank(message = MessageConstant.EMAIL + MessageConstant.NOT_NULL)
    @Email(message = MessageConstant.EMAIL + MessageConstant.FORMAT_ERROR)
    @TableField("email")
    @Schema(description = "用户邮箱" , example = "test@qq.com")
    private String email;

    @TableField("user_avatar")
    @Schema(description = "用户头像地址")
    private String userAvatar;

    @Pattern(regexp = "^.{0,100}$", message = MessageConstant.WORD_LIMIT_ERROR)
    @TableField("introduction")
    @Schema(description = "用户简介格式：100 字以内")
    private String introduction;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    @Schema(description = "用户创建时间")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("update_time")
    @Schema(description = "用户修改时间")
    private LocalDateTime updateTime;

    @TableField("status")
    @Schema(description = "用户状态：0-启用，1-禁用")
    private UserStatusEnum userStatus;
}
