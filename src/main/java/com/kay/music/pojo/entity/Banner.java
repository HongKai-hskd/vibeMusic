package com.kay.music.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kay.music.enumeration.BannerStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Kay
 * @date 2025/11/22 15:31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_banner")
public class Banner implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 轮播图 id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long bannerId;

    /**
     * 轮播图 url
     */
    @TableField("banner_url")
    private String bannerUrl;

    /**
     * 轮播图状态：0-启用，1-禁用
     */
    @TableField("status")
    private BannerStatusEnum bannerStatus;
}
