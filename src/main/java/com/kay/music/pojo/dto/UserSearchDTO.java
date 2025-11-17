package com.kay.music.pojo.dto;

import com.kay.music.enumeration.UserStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Kay
 * @date 2025/11/17 19:00
 */
@Data
@Schema(name = "UserSearchDTO", description = "UserSearchDTO")
public class UserSearchDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    @Schema(description = "页码", example = "1")
    private Integer pageNum;

    @NotNull
    @Schema(description = "每页数量", example = "10")
    private Integer pageSize;

    @Schema(description = "用户名", example = "kay")
    private String username;

    @Schema(description = "用户手机号", example = "15970001234")
    private String phone;

    @Schema(description = "用户状态：0-启用，1-禁用" , example = "0")
    private UserStatusEnum userStatus;
}
