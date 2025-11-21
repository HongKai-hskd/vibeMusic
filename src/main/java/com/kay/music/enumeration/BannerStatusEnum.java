package com.kay.music.enumeration;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * @author Kay
 * @date 2025/11/21 10:55
 */
@Getter
public enum BannerStatusEnum {

    ENABLE(0, "启用"),
    DISABLE(1, "禁用");

    @EnumValue
    private final Integer id;
    private final String bannerStatus;

    BannerStatusEnum(Integer id, String bannerStatus) {
        this.id = id;
        this.bannerStatus = bannerStatus;
    }

}
