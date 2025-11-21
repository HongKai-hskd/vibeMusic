package com.kay.music.controller;

import com.kay.music.pojo.dto.ArtistDTO;
import com.kay.music.pojo.vo.ArtistDetailVO;
import com.kay.music.pojo.vo.ArtistVO;
import com.kay.music.result.PageResult;
import com.kay.music.result.Result;
import com.kay.music.service.IArtistService;
import com.kay.music.utils.ThreadLocalUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Kay
 * @date 2025/11/21 21:01
 */
@RestController
@RequestMapping("/artist")
@Slf4j
@RequiredArgsConstructor
@Tag( name="歌手接口")
public class ArtistController {

    private final IArtistService artistService;
    private final ThreadLocalUtil threadLocalUtil;

    /**
     * @Description: 获取所有歌手列表
     * @Author: Kay
     * @date:   2025/11/21 21:06
     */
    @Operation(summary = "获取所有歌手列表")
    @PostMapping("/getAllArtists")
    public Result<PageResult<ArtistVO>> getAllArtists(@RequestBody @Valid ArtistDTO artistDTO) {
        return artistService.getAllArtists(artistDTO);
    }

    /**
     * @Description: 获取随机歌手 , 随机歌手的数量为 10
     * @Author: Kay
     * @date:   2025/11/21 21:06
     */
    @Operation(summary = "获取随机歌手")
    @GetMapping("/getRandomArtists")
    public Result<List<ArtistVO>> getRandomArtists() {
        return artistService.getRandomArtists();
    }

    /**
     * @Description: 获取歌手详情
     * @Author: Kay
     * @date:   2025/11/21 21:10
     */
    @Operation(summary = "获取歌手详情")
    @GetMapping("/getArtistDetail/{id}")
    public Result<ArtistDetailVO> getArtistDetail(@PathVariable("id") Long artistId, HttpServletRequest request) {
        threadLocalUtil.setThreadLocalByToken(request);
        return artistService.getArtistDetail(artistId);
    }
    
    
}
