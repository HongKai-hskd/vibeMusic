package com.kay.music.pojo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * @author Kay
 * @date 2025/11/21 20:42
 */

@Data
@Schema(name = "ArtistAddDTO", description = "ArtistAddDTO")
public class ArtistAddDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "歌手姓名" , example = "周杰伦")
    private String artistName;

    @Schema(description = "歌手性别：0-男，1-女" , example = "0")
    private Integer gender;

    @Schema(description = "歌手出生日期" , example = "2025-11-21")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birth;

    @Schema(description = "歌手所处地区" , example = "江西")
    private String area;

    @Schema(description = "歌手简介" , example = "你好")
    private String introduction;

}
