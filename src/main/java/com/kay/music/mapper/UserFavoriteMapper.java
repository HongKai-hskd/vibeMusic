package com.kay.music.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kay.music.pojo.entity.UserFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author Kay
 * @date 2025/11/20 19:51
 */
@Mapper
public interface UserFavoriteMapper extends BaseMapper<UserFavorite> {

    // 查询用户收藏的所有歌曲ID
    @Select("SELECT song_id FROM tb_user_favorite WHERE user_id = #{userId} AND type = 0")
    List<Long> getFavoriteSongIdsByUserId(@Param("userId") Long userId);
}
