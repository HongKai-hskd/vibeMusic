package com.kay.music.controller;

import com.kay.music.pojo.dto.SongDTO;
import com.kay.music.pojo.vo.SongVO;
import com.kay.music.result.PageResult;
import com.kay.music.result.Result;
import com.kay.music.service.ISongService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Kay
 * @date 2025/11/19 23:23
 */
@RestController
@RequestMapping("/song")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "歌曲接口")
public class SongController {

    private final ISongService songService;

    /**
     * @Description: 获取所有歌曲
     * @Author: Kay
     * @date:   2025/11/19 23:43
     */
    @Operation(summary = "获取所有歌曲")
    @PostMapping("/getAllSongs")
    public Result<PageResult<SongVO>> getAllSongs(@RequestBody @Valid SongDTO songDTO , HttpServletRequest request){
        return songService.getAllSongs(songDTO, request);
    }
}
