package com.kay.music.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Kay
 * @date 2025/11/21 21:35
 */
@Data
@Schema(name = "SongAndArtistDTO", description = "歌曲和歌才DTO类")
public class SongAndArtistDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 页码
     */
    @Schema(description = "页码", example = "1")
    @NotNull
    private Integer pageNum;

    /**
     * 每页数量
     */
    @Schema(description = "每页数量", example = "10")
    @NotNull
    private Integer pageSize;

    /**
     * 歌手
     */
    @Schema(description = "歌手id", example = "1")
    private Long artistId;

    /**
     * 歌曲名
     */
    @Schema(description = "歌曲名称", example = "晴天")
    private String songName;

    /**
     * 专辑
     */
    @Schema(description = "专辑名称", example = "叶惠美")
    private String album;

}
