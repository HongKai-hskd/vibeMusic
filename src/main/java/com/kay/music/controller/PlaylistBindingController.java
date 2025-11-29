package com.kay.music.controller;

import com.kay.music.result.Result;
import com.kay.music.service.IPlaylistBindingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author Kay
 * @date 2025/11/22 16:07
 */
@RestController
@RequestMapping("/playlist-binding")
@RequiredArgsConstructor
@Tag(name = "歌单绑定接口", description = "歌单和歌曲的绑定关系管理")
public class PlaylistBindingController {

    private final IPlaylistBindingService playlistBindingService;

    /**
     * 添加歌曲到歌单（仅创建者可操作）
     * @param playlistId 歌单 id
     * @param songId 歌曲 id
     * @return Result
     */
    @PostMapping("/add")
    @Operation(summary = "添加歌曲到歌单", description = "只有歌单创建者才能添加歌曲到歌单")
    public Result addSongToPlaylist(
            @Parameter(description = "歌单 ID", required = true) @RequestParam Long playlistId,
            @Parameter(description = "歌曲 ID", required = true) @RequestParam Long songId) {
        return playlistBindingService.addSongToPlaylist(playlistId, songId);
    }

    /**
     * 从歌单移除歌曲（仅创建者可操作）
     * @param playlistId 歌单 id
     * @param songId 歌曲 id
     * @return Result
     */
    @DeleteMapping("/remove")
    @Operation(summary = "从歌单移除歌曲", description = "只有歌单创建者才能从歌单移除歌曲")
    public Result removeSongFromPlaylist(
            @Parameter(description = "歌单 ID", required = true) @RequestParam Long playlistId,
            @Parameter(description = "歌曲 ID", required = true) @RequestParam Long songId) {
        return playlistBindingService.removeSongFromPlaylist(playlistId, songId);
    }
}
