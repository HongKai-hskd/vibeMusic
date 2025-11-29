package com.kay.music.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kay.music.pojo.entity.PlaylistBinding;
import com.kay.music.result.Result;

/**
 * @author Kay
 * @date 2025/11/29
 */
public interface IPlaylistBindingService extends IService<PlaylistBinding> {
    
    /**
     * 添加歌曲到歌单（仅创建者可操作）
     * @param playlistId 歌单 id
     * @param songId 歌曲 id
     * @return Result
     */
    Result addSongToPlaylist(Long playlistId, Long songId);
    
    /**
     * 从歌单移除歌曲（仅创建者可操作）
     * @param playlistId 歌单 id
     * @param songId 歌曲 id
     * @return Result
     */
    Result removeSongFromPlaylist(Long playlistId, Long songId);
}
