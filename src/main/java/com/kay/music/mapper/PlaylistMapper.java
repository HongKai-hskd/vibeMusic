package com.kay.music.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kay.music.pojo.entity.Playlist;
import com.kay.music.pojo.vo.PlaylistDetailVO;
import com.kay.music.pojo.vo.PlaylistVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author Kay
 * @date 2025/11/22 15:09
 */
@Mapper
public interface PlaylistMapper extends BaseMapper<Playlist> {

    // 根据歌单id获取歌单详情
    PlaylistDetailVO getPlaylistDetailById(Long playlistId);

    // 随机推荐歌单
    @Select("""
            SELECT 
                p.id AS playlistId, 
                p.title AS title, 
                p.cover_url AS coverUrl
            FROM tb_playlist p
            ORDER BY RAND() 
            LIMIT #{limit}
            """)
    List<PlaylistVO> getRandomPlaylists(int limit);

    // 根据用户收藏的歌单id列表获取歌单列表
    IPage<PlaylistVO> getPlaylistsByIds(
            Long userId,
            Page<PlaylistVO> page,
            @Param("playlistIds") List<Long> playlistIds,
            @Param("title") String title);
}
