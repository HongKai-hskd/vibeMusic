package com.kay.music.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Kay
 * @date 2025/11/22 15:45
 */
@Data
@Schema(name = "FeedbackDTO", description = "反馈DTO类")
public class FeedbackDTO implements Serializable {

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
     * 反馈内容关键字
     */
    @Schema(description = "反馈内容关键字", example = "建议")
    private String keyword;

}
