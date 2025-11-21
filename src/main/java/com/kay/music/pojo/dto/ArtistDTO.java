package com.kay.music.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Kay
 * @date 2025/11/21 11:24
 */
@Data
@Schema(name = "ArtistDTO", description = "歌手DTO类")
public class ArtistDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    @Schema(description = "页码" , example = "1")
    private Integer pageNum ;

    @NotNull
    @Schema(description = "每页数量" , example = "10")
    private Integer pageSize ;

    @Schema(description = "歌手姓名")
    private String artistName;

    @Schema(description = "歌手性别：0-男，1-女")
    private Integer gender;

    @Schema(description = "歌手所处地区")
    private String area;

}
