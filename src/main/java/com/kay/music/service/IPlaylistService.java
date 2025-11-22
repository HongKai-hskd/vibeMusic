package com.kay.music.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kay.music.pojo.dto.PlaylistAddDTO;
import com.kay.music.pojo.dto.PlaylistDTO;
import com.kay.music.pojo.dto.PlaylistUpdateDTO;
import com.kay.music.pojo.entity.Playlist;
import com.kay.music.result.PageResult;
import com.kay.music.result.Result;

import java.util.List;

/**
 * @author Kay
 * @date 2025/11/22 15:07
 */
public interface IPlaylistService extends IService<Playlist> {
    Result<Long> getAllPlaylistsCount(String style);

    Result<PageResult<Playlist>> getAllPlaylistsInfo(PlaylistDTO playlistDTO);

    Result addPlaylist(PlaylistAddDTO playlistAddDTO);

    Result updatePlaylist(PlaylistUpdateDTO playlistUpdateDTO);

    Result updatePlaylistCover(Long playlistId, String coverUrl);

    Result deletePlaylist(Long playlistId);

    Result deletePlaylists(List<Long> playlistIds);
}
