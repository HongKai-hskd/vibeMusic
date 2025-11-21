package com.kay.music.enumeration;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * @author Kay
 * @date 2025/11/21 10:54
 */
@Getter
public enum FavoriteTypeEnum {

    SONG(0, "歌曲收藏"),
    PLAYLIST(1, "歌单收藏");

    @EnumValue
    private final Integer id;
    private final String favoriteType;

    FavoriteTypeEnum(Integer id, String favoriteType) {
        this.id = id;
        this.favoriteType = favoriteType;
    }

}