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
 * @date 2025/11/21 21:10
 */
@Data
public class ArtistDetailVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    
    @Schema(description = "歌手 id", example = "1")
    private Long artistId;

    @Schema(description = "歌手姓名")
    private String artistName;

    @Schema(description = "歌手性别：0-男，1-女")
    private Integer gender;

    @Schema(description = "歌手头像")
    private String avatar;

    @Schema(description = "歌手出生日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birth;

    @Schema(description = "歌手所处地区")
    private String area;

    @Schema(description = "歌手简介")
    private String introduction;

    @Schema(description = "歌曲列表")
    private List<SongVO> songs;

}
