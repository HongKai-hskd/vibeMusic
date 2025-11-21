package com.kay.music.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kay.music.pojo.entity.Genre;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Kay
 * @date 2025/11/21 21:45
 */
@Mapper
public interface GenreMapper extends BaseMapper<Genre> {
}
