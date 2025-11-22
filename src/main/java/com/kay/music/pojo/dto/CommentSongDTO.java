package com.kay.music.pojo.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Kay
 * @date 2025/11/22 15:45
 */
@Data
public class CommentSongDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 歌曲id
     */
    private Long songId;

    /**
     * 评论内容
     */
    private String content;

}
