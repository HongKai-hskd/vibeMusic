package com.kay.music.pojo.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Kay
 * @date 2025/11/22 15:44
 */
@Data
public class CommentPlaylistDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 歌单id
     */
    private Long playlistId;

    /**
     * 评论内容
     */
    private String content;

}