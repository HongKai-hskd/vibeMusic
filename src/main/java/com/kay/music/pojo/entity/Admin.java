package com.kay.music.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kay.music.constant.MessageConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Kay
 * @date 2025/11/15 20:58
 */
@Data
@TableName("tb_admin")
@EqualsAndHashCode(callSuper = false)
// callSuper = false 表示：
// 如果这个类继承了父类，equals / hashCode 不会把父类的字段也算进去
@Accessors(chain = true)
/* 开启 链式调用：
Admin admin = new Admin()
        .setUsername("admin")
        .setPassword("123456");*/
@Schema(name = "Admin", description = "管理员实体类")
public class Admin implements Serializable {

    // 不能序列化 = 不能存 Session、不能走网络传输、不能写对象到文件
    // 一般建议 序列化
    // @Serial：JDK 14+ 提供的注解，用来标注这是一个「序列化相关的字段或方法」，更规范。
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id" , type = IdType.AUTO)
    @Schema(description = "管理员ID", example = "1")
    private Long adminId;

    @TableField("username")
    /**
     * 来自 Jakarta Validation（Bean Validation）
     * 约束：username 不能为空 & 不能是全空格
     * 常在 Controller 的入参上配合 @Valid
     * 如果前端传 username = null 或 "" 或 " "，就会校验失败，抛出异常。
     */
    @NotBlank(message = MessageConstant.USERNAME + MessageConstant.NOT_NULL)
    // 用户名必须是 3~16 位，只能由 字母、数字、下划线、连字符 组成。
    @Pattern(regexp = "^[a-zA-Z0-9_-]{3,16}$", message = MessageConstant.USERNAME + MessageConstant.FORMAT_ERROR)
    @Schema(description = "管理员用户名（3-16位 字母/数字/下划线/连字符）", example = "admin")
    private String username;

    @TableField("password")
    // 密码不能为空、不能全是空格
    @NotBlank(message = MessageConstant.PASSWORD + MessageConstant.NOT_NULL)
    // 密码格式：3~16 位，只能由 字母、数字、下划线、连字符 组成。
    @Pattern(regexp = "^[a-zA-Z0-9_-]{3,18}$", message = MessageConstant.PASSWORD + MessageConstant.FORMAT_ERROR)
    @Schema(description = "管理员密码（3-18位 字母/数字/下划线/连字符）", example = "123456")
    private String password;

}
