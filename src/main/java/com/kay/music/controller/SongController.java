package com.kay.music.controller;

import com.kay.music.enumeration.RoleEnum;
import com.kay.music.pojo.dto.SongDTO;
import com.kay.music.pojo.vo.SongVO;
import com.kay.music.result.PageResult;
import com.kay.music.result.Result;
import com.kay.music.service.ISongService;
import com.kay.music.utils.JwtUtil;
import com.kay.music.utils.ThreadLocalUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    private final JwtUtil jwtUtil;
    private final ThreadLocalUtil threadLocalUtil;

    /**
     * @Description: 获取所有歌曲 , 区分了 用户登录 ， 无用户登录 时， 缓存问题（拆成了两个函数）
     * @Author: Kay
     * @date:   2025/11/19 23:43
     */
    @Operation(summary = "获取所有歌曲")
    @PostMapping("/getAllSongs")
    public Result<PageResult<SongVO>> getAllSongs(@RequestBody @Valid SongDTO songDTO,
                                                  HttpServletRequest request){
        // 因为 这个接口 是登录和不登录都能访问的，所以进行了 放行，就没有校验 jwt ， 当然 ThreadLocal就没有内容了
        // 1. 如果 ThreadLocal 为空，但 header 里有 token，可以手动解析一次
        threadLocalUtil.setThreadLocalByToken(request);

        // 2. 再正常读取 userId / role
        Long userId = ThreadLocalUtil.getUserId();
        String role = ThreadLocalUtil.getRole();
        // 未登录 或 角色不是普通用户 → 游客版
        if (userId == null || !RoleEnum.USER.getRole().equals(role)) {
            return songService.getAllSongsForGuest(songDTO);
        }

        // 登录用户 → 用户版（注意这里传 userId，方便做缓存 key）
        return songService.getAllSongsForUser(songDTO, userId);
    }

    /**
     * @Description: 获取推荐歌曲,推荐歌曲的数量为 20
     * @Author: Kay
     * @date:   2025/11/20 23:22
     */
    @Operation(summary = "获取推荐歌曲")
    @GetMapping("/getRecommendedSongs")
    public Result<List<SongVO>> getRecommendedSongs(HttpServletRequest request) {
        // 1. 如果 ThreadLocal 为空，但 header 里有 token，可以手动解析一次
        threadLocalUtil.setThreadLocalByToken(request);
        return songService.getRecommendedSongs();
    }

}
