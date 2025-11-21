package com.kay.music.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Kay
 * @date 2025/11/21 21:02
 */
@Data
@Schema(name = "ArtistVO", description = "ArtistVO")
public class ArtistVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "歌手id" , example = "1")
    private Long artistId;

    @Schema(description = "歌手姓名" , example = "周")
    private String artistName;

    @Schema(description = "歌手头像")
    private String avatar;

}