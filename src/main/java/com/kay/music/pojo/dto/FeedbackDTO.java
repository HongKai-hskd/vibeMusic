package com.kay.music.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Kay
 * @date 2025/11/22 15:45
 */
@Data
public class FeedbackDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 页码
     */
    @NotNull
    private Integer pageNum;

    /**
     * 每页数量
     */
    @NotNull
    private Integer pageSize;

    /**
     * 反馈内容关键字
     */
    private String keyword;

}
