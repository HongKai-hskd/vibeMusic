package com.kay.music.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kay.music.pojo.dto.SongDTO;
import com.kay.music.pojo.entity.Song;
import com.kay.music.pojo.vo.SongVO;
import com.kay.music.result.PageResult;
import com.kay.music.result.Result;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Kay
 * @date 2025/11/19 23:39
 */
public interface ISongService extends IService<Song> {
    Result<PageResult<SongVO>> getAllSongs(SongDTO songDTO, HttpServletRequest request);
}
