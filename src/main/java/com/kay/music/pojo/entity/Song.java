package com.kay.music.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * @author Kay
 * @date 2025/11/19 23:24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_song")
@Schema(name = "Song", description = "歌曲实体类")
public class Song implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "歌曲 id" , example = "1")
    private Long songId;

    @TableField("artist_id")
    @Schema(description = "歌手 id" , example = "1")
    private Long artistId;

    @TableField("name")
    @Schema(description = "歌名" , example = "晴天")
    private String songName;

    @TableField("album")
    @Schema(description = "专辑" , example = "叶惠美")
    private String album;

    @TableField("lyric")
    @Schema(description = "专辑" , example = "故事的小黄花....")
    private String lyric;

    @TableField("duration")
    @Schema(description = "歌曲时长" , example = "3:40")
    private String duration;

    @TableField("style")
    @Schema(description = "歌曲风格" , example = "emo")
    private String style;

    @TableField("cover_url")
    @Schema(description = "歌曲封面 url" , example = ".....")
    private String coverUrl;

    @TableField("audio_url")
    @Schema(description = "歌曲 url" , example = ".....")
    private String audioUrl;

    @TableField("release_time")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "歌曲发行时间" , example = "2025-11-19")
    private LocalDate releaseTime;

}
