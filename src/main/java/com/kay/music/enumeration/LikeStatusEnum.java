package com.kay.music.enumeration;

import lombok.Getter;

/**
 * @author Kay
 * @date 2025/11/20 19:32
 */
@Getter
public enum LikeStatusEnum {

    DEFAULT(0, "默认"),
    LIKE(1, "喜欢");

    private final Integer id;
    private final String likeStatus;

    LikeStatusEnum(Integer id, String likeStatus) {
        this.id = id;
        this.likeStatus = likeStatus;
    }

}