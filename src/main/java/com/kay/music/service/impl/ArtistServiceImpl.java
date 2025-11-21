package com.kay.music.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kay.music.mapper.ArtistMapper;
import com.kay.music.pojo.entity.Artist;
import com.kay.music.result.Result;
import com.kay.music.service.IArtistService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.stereotype.Service;

/**
 * @author Kay
 * @date 2025/11/21 11:15
 */
@Service
@CacheConfig(cacheNames = "artistCache")
@RequiredArgsConstructor
public class ArtistServiceImpl extends ServiceImpl<ArtistMapper, Artist> implements IArtistService {

    private final ArtistMapper artistMapper;

    
    /**
     * @Description:  获取所有歌手数量
     * @Author: Kay
     * @date:   2025/11/21 11:18
     */
    @Override
    public Result<Long> getAllArtistsCount(Integer gender, String area) {
        LambdaQueryWrapper<Artist> wrapper = new LambdaQueryWrapper<>();

        if (gender != null) {
            wrapper.eq(Artist::getGender, gender);
        }
        if (area != null) {
            wrapper.eq(Artist::getArea, area);
        }

        return Result.success(artistMapper.selectCount(wrapper));
    }
}
