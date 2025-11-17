package com.kay.music.pojo.vo;

import com.kay.music.enumeration.UserStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author Kay
 * @date 2025/11/17 19:03
 */
@Data
@Schema(name = "UserManagementVO", description = "用户管理列表展示对象")
public class UserManagementVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "用户名（3-16位 字母/数字/下划线/连字符）", example = "kay")
    private String username;

    @Schema(description = "手机号（1开头，11位数字）", example = "15970001234")
    private String phone;

    @Schema(description = "邮箱地址", example = "user@example.com")
    private String email;

    @Schema(description = "用户头像 URL 地址", example = "https://cdn.xxx.com/avatar.jpg")
    private String userAvatar;

    @Schema(description = "用户简介（100 字以内）", example = "这个人很懒，没有写简介。")
    private String introduction;

    @Schema(description = "用户创建时间", example = "2025-01-01 12:00:00")
    private LocalDateTime createTime;

    @Schema(description = "用户修改时间", example = "2025-01-05 15:30:00")
    private LocalDateTime updateTime;

    @Schema(description = "用户状态（0-启用，1-禁用）", example = "0")
    private UserStatusEnum userStatus;
}
