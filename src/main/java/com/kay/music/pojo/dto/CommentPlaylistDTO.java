package com.kay.music.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Kay
 * @date 2025/11/22 15:44
 */
@Data
@Schema(name = "CommentPlaylistDTO", description = "歌单评论DTO")
public class CommentPlaylistDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 歌单id
     */
    @Schema(description = "歌单id", example = "1")
    private Long playlistId;

    /**
     * 评论内容
     */
    @Schema(description = "评论内容", example = "这歌单666")
    private String content;

}