package com.kay.music.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kay.music.pojo.entity.UserFavorite;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Kay
 * @date 2025/11/20 19:51
 */
@Mapper
public interface UserFavoriteMapper extends BaseMapper<UserFavorite> {
}
