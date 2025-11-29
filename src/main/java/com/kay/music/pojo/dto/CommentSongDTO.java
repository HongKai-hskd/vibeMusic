package com.kay.music.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Kay
 * @date 2025/11/22 15:45
 */
@Data
@Schema(name = "CommentSongDTO", description = "歌曲评论DTO类")
public class CommentSongDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 歌曲id
     */
    @Schema(description = "歌曲id", example = "1")
    private Long songId;

    /**
     * 评论内容
     */
    @Schema(description = "评论内容", example = "这首歌太好听了")
    private String content;

}
