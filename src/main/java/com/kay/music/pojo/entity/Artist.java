package com.kay.music.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * @author Kay
 * @date 2025/11/21 11:12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_artist")
@Schema(name = "Artist", description = "歌手实体类")
public class Artist implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "歌手 id", example = "1")
    @TableId(value = "id", type = IdType.AUTO)
    private Long artistId;

    @Schema(description = "歌手姓名")
    @TableField("name")
    private String artistName;

    @Schema(description = "歌手性别：0-男，1-女")
    @TableField("gender")
    private Integer gender;

    @Schema(description = "歌手头像")
    @TableField("avatar")
    private String avatar;

    @Schema(description = "歌手出生日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @TableField("birth")
    private LocalDate birth;

    @Schema(description = "歌手所处地区")
    @TableField("area")
    private String area;

    @Schema(description = "歌手简介")
    @TableField("introduction")
    private String introduction;

}