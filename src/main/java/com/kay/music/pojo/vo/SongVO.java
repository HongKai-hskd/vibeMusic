package com.kay.music.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * @author Kay
 * @date 2025/11/19 23:31
 */
@Data
@Schema(name = "SongVO", description = "歌曲VO类")
public class SongVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "歌曲 id" , example = "1")
    private Long songId;

    @Schema(description = "歌名" , example = "晴天")
    private String songName;

    @Schema(description = "歌手名" , example = "周杰伦")
    private String artistName;

    @Schema(description = "专辑" , example = "叶惠美")
    private String album;

    @Schema(description = "歌曲时长" , example = "3:40")
    private String duration;

    @Schema(description = "歌曲封面 url" , example = ".....")
    private String coverUrl;

    @Schema(description = "歌曲 url" , example = ".....")
    private String audioUrl;


    @Schema(description = "喜欢状态，0：默认，1：喜欢" , example = "0")
    private Integer likeStatus;

    @Schema(description = "歌曲发行时间" , example = "2025-11-19")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseTime;
}
