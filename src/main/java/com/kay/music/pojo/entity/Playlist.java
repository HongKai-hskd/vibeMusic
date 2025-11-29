package com.kay.music.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Kay
 * @date 2025/11/22 15:08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_playlist")
public class Playlist implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 歌单 id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long playlistId;

    /**
     * 歌单标题
     */
    @TableField("title")
    private String title;

    /**
     * 歌单封面
     */
    @TableField("cover_url")
    private String coverUrl;

    /**
     * 歌单简介
     */
    @TableField("introduction")
    private String introduction;

    /**
     * 歌单风格
     */
    @TableField("style")
    private String style;

    /**
     * 创建者用户 id
     */
    @TableField("user_id")
    private Long userId;

}
