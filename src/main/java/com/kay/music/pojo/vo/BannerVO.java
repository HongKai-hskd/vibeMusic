package com.kay.music.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Kay
 * @date 2025/11/22 15:36
 */
@Data
@Schema(name = "BannerVO", description = "轮播图VO类")
public class BannerVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 轮播图 id
     */
    @Schema(description = "轮播图id")
    private Long bannerId;

    /**
     * 轮播图 url
     */
    @Schema(description = "轮播图URL")
    private String bannerUrl;

}