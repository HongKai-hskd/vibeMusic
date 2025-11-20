package com.kay.music.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kay.music.pojo.entity.Song;
import com.kay.music.pojo.vo.SongVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author Kay
 * @date 2025/11/19 23:40
 */
@Mapper
public interface SongMapper extends BaseMapper<Song> {

    /**
     * 如果 songName == null ，则 (null IS NULL) 为 true → 该条件不生效
     * 如果有值，则执行模糊查询 LIKE %xxx%
     * 无需 <if> 标签也能实现动态 where 条件
     */
    // 获取歌曲列表 ， 只对传入参数不为空的字段做模糊匹配搜索
    @Select("""
                SELECT 
                    s.id AS songId, 
                    s.name AS songName, 
                    s.album, 
                    s.duration, 
                    s.cover_url AS coverUrl, 
                    s.audio_url AS audioUrl, 
                    s.release_time AS releaseTime, 
                    a.name AS artistName
                FROM tb_song s
                LEFT JOIN tb_artist a ON s.artist_id = a.id
                WHERE 
                    (#{songName} IS NULL OR s.name LIKE CONCAT('%', #{songName}, '%'))
                    AND (#{artistName} IS NULL OR a.name LIKE CONCAT('%', #{artistName}, '%'))
                    AND (#{album} IS NULL OR s.album LIKE CONCAT('%', #{album}, '%'))
            """)
    IPage<SongVO> getSongsWithArtist(Page<SongVO> page,
                                     @Param("songName") String songName,
                                     @Param("artistName") String artistName,
                                     @Param("album") String album);
}
