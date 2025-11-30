package com.kay.music.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Kay
 * @date 2025/11/22 15:17
 */
@Data
@Schema(name = "PlaylistAddDTO", description = "歌单添加DTO类")
public class PlaylistAddDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 歌单标题
     */
    @Schema(description = "歌单标题", example = "华语经典")
    private String title;

    /**
     * 歌单简介
     */
    @Schema(description = "歌单简介", example = "收录华语经典歌曲")
    private String introduction;

}