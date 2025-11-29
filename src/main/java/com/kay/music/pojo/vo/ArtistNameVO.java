package com.kay.music.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Kay
 * @date 2025/11/21 21:32
 */
@Data
@Schema(name = "ArtistNameVO", description = "歌手名称VO类")
public class ArtistNameVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 歌手 id
     */
    @Schema(description = "歌手id")
    private Long artistId;

    /**
     * 歌手姓名
     */
    @Schema(description = "歌手姓名")
    private String artistName;

}
