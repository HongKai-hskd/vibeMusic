package com.kay.music.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kay.music.pojo.dto.PlaylistDTO;
import com.kay.music.pojo.dto.SongDTO;
import com.kay.music.pojo.entity.UserFavorite;
import com.kay.music.pojo.vo.PlaylistVO;
import com.kay.music.pojo.vo.SongVO;
import com.kay.music.result.PageResult;
import com.kay.music.result.Result;

/**
 * @author Kay
 * @date 2025/11/22 16:29
 */
public interface IUserFavoriteService extends IService<UserFavorite>  {
    Result<PageResult<SongVO>> getUserFavoriteSongs(SongDTO songDTO);

    Result collectSong(Long songId);

    Result cancelCollectSong(Long songId);

    Result<PageResult<PlaylistVO>> getUserFavoritePlaylists(PlaylistDTO playlistDTO);

    Result collectPlaylist(Long playlistId);

    Result cancelCollectPlaylist(Long playlistId);
}
