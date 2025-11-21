package com.kay.music.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kay.music.constant.MessageConstant;
import com.kay.music.mapper.ArtistMapper;
import com.kay.music.pojo.dto.ArtistDTO;
import com.kay.music.pojo.entity.Artist;
import com.kay.music.result.PageResult;
import com.kay.music.result.Result;
import com.kay.music.service.IArtistService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
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

    /**
     * @Description: 获取所有歌手列表（含详情）
     * @Author: Kay
     * @date:   2025/11/21 11:31
     */
    @Override
    @Cacheable(key = "#artistDTO.pageNum + '-' + #artistDTO.pageSize + '-' + #artistDTO.artistName + '-' + #artistDTO.gender + '-' + #artistDTO.area + '-admin'")
    public Result<PageResult<Artist>> getAllArtistsAndDetail(ArtistDTO artistDTO) {

        // 分页查询
        Page<Artist> page = new Page<>(artistDTO.getPageNum(), artistDTO.getPageSize());
        LambdaQueryWrapper<Artist> queryWrapper = new LambdaQueryWrapper<>();
        // 根据 artistDTO 的条件构建查询条件
        if (artistDTO.getArtistName() != null) {
            queryWrapper.like(Artist::getArtistName, artistDTO.getArtistName());
        }
        if (artistDTO.getGender() != null) {
            queryWrapper.eq(Artist::getGender, artistDTO.getGender());
        }
        if (artistDTO.getArea() != null) {
            queryWrapper.like(Artist::getArea, artistDTO.getArea());
        }
        // 倒序排序
        queryWrapper.orderByDesc(Artist::getArtistId);

        IPage<Artist> artistPage = artistMapper.selectPage(page, queryWrapper);
        if (artistPage.getRecords().isEmpty()) {
            return Result.success(MessageConstant.DATA_NOT_FOUND, new PageResult<>(0L, null));
        }
        return Result.success(new PageResult<>(artistPage.getTotal(), artistPage.getRecords()));
    }




}
