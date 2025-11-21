package com.kay.music.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kay.music.pojo.entity.Artist;
import com.kay.music.result.Result;

/**
 * @author Kay
 * @date 2025/11/21 11:11
 */
public interface IArtistService extends IService<Artist> {
    Result<Long> getAllArtistsCount(Integer gender, String area);
}
