package com.kay.music.pojo.vo;

/**
 * @author Kay
 * @date 2025/11/19 11:39
 */

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


@Data
@Schema(name = "UserVO", description = "用户VO类实体")
public class UserVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "用户名（3-16位 字母/数字/下划线/连字符）", example = "kay")
    private String username;

    @Schema(description = "手机号格式：1开头，11位数字", example = "15970001234")
    private String phone;

    @Schema(description = "用户邮箱" , example = "test@qq.com")
    private String email;

    @Schema(description = "用户头像地址")
    private String userAvatar;

    @Schema(description = "用户简介格式：100 字以内")
    private String introduction;

}
