package com.kay.music.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Kay
 * @date 2025/11/19 23:35
 */
@Data
@Schema(name = "SongDTO", description = "歌曲DTO类")
public class SongDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "页码" , example = "1")
    @NotNull
    private Integer pageNum;

    @Schema(description = "每页数量" , example = "10")
    @NotNull
    private Integer pageSize;

    @Schema(description = "歌曲名" , example = "晴天")
    private String songName;

    @Schema(description = "歌手" , example = "周杰伦")
    private String artistName;

    @Schema(description = "专辑" , example = "叶惠美")
    private String album;

}
