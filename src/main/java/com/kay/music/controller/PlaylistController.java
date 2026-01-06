package com.kay.music.controller;

import com.kay.music.pojo.dto.PlaylistAddDTO;
import com.kay.music.pojo.dto.PlaylistDTO;
import com.kay.music.pojo.vo.PlaylistDetailVO;
import com.kay.music.pojo.vo.PlaylistVO;
import com.kay.music.result.PageResult;
import com.kay.music.result.Result;
import com.kay.music.service.IPlaylistService;
import com.kay.music.service.MinioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author Kay
 * @date 2025/11/22 16:09
 */
@RestController
@RequestMapping("/playlist")
@RequiredArgsConstructor
@Tag(name = "歌单接口")
public class PlaylistController {


    private final IPlaylistService playlistService;
    private final MinioService minioService;

    /**
     * 获取所有歌单
     *
     * @param playlistDTO playlistDTO
     * @return 歌单列表
     */
    @Operation(summary = "获取所有歌单")
    @PostMapping("/getAllPlaylists")
    public Result<PageResult<PlaylistVO>> getAllPlaylists(@RequestBody @Valid PlaylistDTO playlistDTO) {
        return playlistService.getAllPlaylists(playlistDTO);
    }

    /**
     * 获取推荐歌单
     *
     * @param request request
     * @return 推荐歌单列表
     */
    @Operation(summary = "获取推荐歌单")
    @GetMapping("/getRecommendedPlaylists")
    public Result<List<PlaylistVO>> getRandomPlaylists(HttpServletRequest request) {
        return playlistService.getRecommendedPlaylists(request);
    }

    /**
     * 获取歌单详情
     *
     * @param playlistId 歌单id
     * @return 歌单详情
     */
    @Operation(summary = "获取歌单详情")
    @GetMapping("/getPlaylistDetail/{id}")
    public Result<PlaylistDetailVO> getPlaylistDetail(@PathVariable("id") Long playlistId, HttpServletRequest request) {
        return playlistService.getPlaylistDetail(playlistId, request);
    }


    /**
     * @Description: 新增歌单
     * @Author: Kay
     */
    @Operation(summary = "新增歌单")
    @PostMapping("/addPlaylist")
    public Result addPlaylist(@RequestBody PlaylistAddDTO playlistAddDTO) {
        return playlistService.addPlaylist(playlistAddDTO);
    }


    /**
     * @Description: 更新歌单封面
     * @Author: Kay
     * @date:   2025/11/22 15:22
     */
    @Operation(summary = "更新歌单封面")
    @PatchMapping("/updatePlaylistCover/{id}")
    public Result updatePlaylistCover(@PathVariable("id") Long playlistId, @RequestParam("cover") MultipartFile cover) {
        String coverUrl = minioService.uploadFile(cover, "playlists");  // 上传到 playlists 目录
        return playlistService.updatePlaylistCover(playlistId, coverUrl);
    }


}
