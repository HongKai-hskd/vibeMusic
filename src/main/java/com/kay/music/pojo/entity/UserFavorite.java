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
import java.time.LocalDateTime;

/**
 * @author Kay
 * @date 2025/11/20 19:47
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_user_favorite")
@Schema(name = "UserFavorite", description = "用户喜欢列表实体")
public class UserFavorite implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long userFavoriteId;

    @Schema(description = "用户 id")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "收藏类型：0-歌曲，1-歌单")
    @TableField("type")
    private Integer type;

    @Schema(description = "收藏歌曲 id")
    @TableField("song_id")
    private Long songId;

    @Schema(description = "收藏歌单 id")
    @TableField("playlist_id")
    private Long playlistId;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;

}
