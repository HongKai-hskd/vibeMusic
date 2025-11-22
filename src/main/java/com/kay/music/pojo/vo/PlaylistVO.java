package com.kay.music.pojo.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Kay
 * @date 2025/11/22 15:48
 */

@Data
public class PlaylistVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 歌单 id
     */
    private Long playlistId;

    /**
     * 歌单标题
     */
    private String title;

    /**
     * 歌单封面
     */
    private String coverUrl;

}
