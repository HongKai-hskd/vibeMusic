package com.kay.music.pojo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * @author Kay
 * @date 2025/11/21 21:40
 */
@Data
@Schema(name = "SongAddDTO", description = "歌曲添加DTO类")
public class SongAddDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 歌手 id
     */
    @Schema(description = "歌手id", example = "1")
    private Long artistId;

    /**
     * 歌名
     */
    @Schema(description = "歌曲名称", example = "晴天")
    private String songName;

    /**
     * 专辑
     */
    @Schema(description = "专辑名称", example = "叶惠美")
    private String album;

    /**
     * 歌曲发行时间
     */
    @Schema(description = "歌曲发行时间", example = "2003-07-31")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseTime;

}