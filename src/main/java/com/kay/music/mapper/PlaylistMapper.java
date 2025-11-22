package com.kay.music.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kay.music.pojo.entity.Playlist;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Kay
 * @date 2025/11/22 15:09
 */
@Mapper
public interface PlaylistMapper extends BaseMapper<Playlist> {
}
