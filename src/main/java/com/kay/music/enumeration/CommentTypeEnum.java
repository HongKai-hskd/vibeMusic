package com.kay.music.enumeration;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * @author Kay
 * @date 2025/11/21 10:55
 */
@Getter
public enum CommentTypeEnum {

    SONG(0, "歌曲评论"),
    PLAYLIST(1, "歌单评论");

    @EnumValue
    private final Integer id;
    private final String commentType;

    CommentTypeEnum(Integer id, String commentType) {
        this.id = id;
        this.commentType = commentType;
    }

}