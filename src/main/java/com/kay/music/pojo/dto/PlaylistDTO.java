package com.kay.music.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Kay
 * @date 2025/11/22 15:14
 */
@Data
@Schema(name = "PlaylistDTO", description = "歌单DTO类")
public class PlaylistDTO implements Serializable {

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
     * 歌单标题
     */
    @Schema(description = "歌单标题", example = "华语经典")
    private String title;

}
