package com.kay.music.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Kay
 * @date 2025/11/22 15:48
 */
@Data
@Schema(name = "PlaylistVO", description = "歌单VO类")
public class PlaylistVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 歌单 id
     */
    @Schema(description = "歌单id")
    private Long playlistId;

    /**
     * 歌单标题
     */
    @Schema(description = "歌单标题")
    private String title;

    /**
     * 歌单封面
     */
    @Schema(description = "歌单封面URL")
    private String coverUrl;

}
