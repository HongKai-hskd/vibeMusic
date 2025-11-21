package com.kay.music.pojo.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Kay
 * @date 2025/11/21 21:32
 */
@Data
public class ArtistNameVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 歌手 id
     */
    private Long artistId;

    /**
     * 歌手姓名
     */
    private String artistName;

}
