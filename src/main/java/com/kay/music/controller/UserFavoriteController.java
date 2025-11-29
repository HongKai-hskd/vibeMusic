package com.kay.music.controller;

import com.kay.music.pojo.dto.PlaylistDTO;
import com.kay.music.pojo.dto.SongDTO;
import com.kay.music.pojo.vo.PlaylistVO;
import com.kay.music.pojo.vo.SongVO;
import com.kay.music.result.PageResult;
import com.kay.music.result.Result;
import com.kay.music.service.IUserFavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author Kay
 * @date 2025/11/22 16:29
 */
@RestController
@RequestMapping("/favorite")
@RequiredArgsConstructor
@Tag(name = "用户收藏接口")
public class UserFavoriteController {


    private final IUserFavoriteService userFavoriteService;

    /**
     * 获取用户收藏的歌曲列表
     *
     * @return 用户收藏的歌曲列表
     */
    @Operation(summary = "获取用户收藏的歌曲列表")
    @PostMapping("/getFavoriteSongs")
    public Result<PageResult<SongVO>> getUserFavoriteSongs(@RequestBody @Valid SongDTO songDTO) {
        return userFavoriteService.getUserFavoriteSongs(songDTO);
    }

    /**
     * 收藏歌曲
     *
     * @param songId 歌曲id
     * @return 收藏结果
     */
    @Operation(summary = "收藏歌曲")
    @PostMapping("/collectSong")
    public Result collectSong(@RequestParam Long songId) {
        return userFavoriteService.collectSong(songId);
    }

    /**
     * 取消收藏歌曲
     *
     * @param songId 歌曲id
     * @return 取消收藏结果
     */
    @Operation(summary = "取消收藏歌曲")
    @DeleteMapping("/cancelCollectSong")
    public Result cancelCollectSong(@RequestParam Long songId) {
        return userFavoriteService.cancelCollectSong(songId);
    }

    /**
     * 获取用户收藏的歌单列表
     *
     * @return 用户收藏的歌单列表
     */
    @Operation(summary = "获取用户收藏的歌单列表")
    @PostMapping("/getFavoritePlaylists")
    public Result<PageResult<PlaylistVO>> getFavoritePlaylists(@RequestBody @Valid PlaylistDTO playlistDTO) {
        return userFavoriteService.getUserFavoritePlaylists(playlistDTO);
    }

    /**
     * 收藏歌单
     *
     * @param playlistId 歌单id
     * @return 收藏结果
     */
    @Operation(summary = "收藏歌单")
    @PostMapping("/collectPlaylist")
    public Result collectPlaylist(@RequestParam Long playlistId) {
        return userFavoriteService.collectPlaylist(playlistId);
    }

    /**
     * 取消收藏歌单
     *
     * @param playlistId 歌单id
     * @return 取消收藏结果
     */
    @Operation(summary = "取消收藏歌单")
    @DeleteMapping("/cancelCollectPlaylist")
    public Result cancelCollectPlaylist(@RequestParam Long playlistId) {
        return userFavoriteService.cancelCollectPlaylist(playlistId);
    }
}
