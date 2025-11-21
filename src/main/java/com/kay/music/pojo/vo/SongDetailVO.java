package com.kay.music.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * @author Kay
 * @date 2025/11/21 10:27
 */


@Data
@Schema(name = "SongDetailVO", description = "歌曲细节VO类")
public class SongDetailVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "歌曲 id")
    private Long songId;

    @Schema(description = "歌名")
    private String songName;

    @Schema(description = "歌手")
    private String artistName;

    @Schema(description = "专辑")
    private String album;

    @Schema(description = "歌词")
    private String lyric;

    @Schema(description = "歌曲时长")
    private String duration;

    @Schema(description = "歌曲封面 url")
    private String coverUrl;

    @Schema(description = "歌曲 url")
    private String audioUrl;

    @Schema(description = "歌曲发行时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseTime;

    @Schema(description = "喜欢状态，0：默认，1：喜欢")
    private Integer likeStatus;

    @Schema(description = "评论列表")
    private List<CommentVO> comments;
}
