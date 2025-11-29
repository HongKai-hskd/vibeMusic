package com.kay.music.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author Kay
 * @date 2025/11/22 15:48
 */
@Data
@Schema(name = "PlaylistDetailVO", description = "歌单详情VO类")
public class PlaylistDetailVO implements Serializable {

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

    /**
     * 歌单简介
     */
    @Schema(description = "歌单简介")
    private String introduction;

    /**
     * 歌曲列表
     */
    @Schema(description = "歌曲列表")
    private List<SongVO> songs;

    /**
     * 喜欢状态
     * 0：默认
     * 1：喜欢
     */
    @Schema(description = "喜欢状态 0-默认 1-喜欢")
    private Integer likeStatus;

    /**
     * 评论列表
     */
    @Schema(description = "评论列表")
    private List<CommentVO> comments;

}
