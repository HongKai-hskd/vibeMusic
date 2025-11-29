package com.kay.music.pojo.dto;

import com.kay.music.enumeration.BannerStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Kay
 * @date 2025/11/22 15:34
 */
@Data
@Schema(name = "BannerDTO", description = "轮播图DTO类")
public class BannerDTO implements Serializable {

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
     * 轮播图状态：0-启用，1-禁用
     */
    @Schema(description = "轮播图状态：0-启用，1-禁用")
    private BannerStatusEnum bannerStatus;

}
