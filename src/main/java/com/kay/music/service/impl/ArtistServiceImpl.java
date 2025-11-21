package com.kay.music.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kay.music.constant.MessageConstant;
import com.kay.music.enumeration.LikeStatusEnum;
import com.kay.music.mapper.ArtistMapper;
import com.kay.music.mapper.UserFavoriteMapper;
import com.kay.music.pojo.dto.ArtistAddDTO;
import com.kay.music.pojo.dto.ArtistDTO;
import com.kay.music.pojo.dto.ArtistUpdateDTO;
import com.kay.music.pojo.entity.Artist;
import com.kay.music.pojo.entity.UserFavorite;
import com.kay.music.pojo.vo.ArtistDetailVO;
import com.kay.music.pojo.vo.ArtistVO;
import com.kay.music.pojo.vo.SongVO;
import com.kay.music.result.PageResult;
import com.kay.music.result.Result;
import com.kay.music.service.IArtistService;
import com.kay.music.service.MinioService;
import com.kay.music.utils.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Kay
 * @date 2025/11/21 11:15
 */
@Service
@CacheConfig(cacheNames = "artistCache")
@RequiredArgsConstructor
public class ArtistServiceImpl extends ServiceImpl<ArtistMapper, Artist> implements IArtistService {

    private final ArtistMapper artistMapper;
    private final MinioService minioService;
    private final UserFavoriteMapper userFavoriteMapper;

    
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

    /**
     * @Description: 添加歌手
     * @Author: Kay
     * @date:   2025/11/21 20:46
     */
    @Override
    @CacheEvict(cacheNames = "artistCache", allEntries = true)
    public Result addArtist(ArtistAddDTO artistAddDTO) {

        LambdaQueryWrapper<Artist> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Artist::getArtistName, artistAddDTO.getArtistName());
        // 1. 不能重名
        // TODO 这里后期可以优化，歌手应该是可以重名才对的
        if (artistMapper.selectCount(queryWrapper) > 0) {
            return Result.error(MessageConstant.ARTIST + MessageConstant.ALREADY_EXISTS);
        }

        Artist artist = new Artist();
        BeanUtils.copyProperties(artistAddDTO, artist);
        artistMapper.insert(artist);

        return Result.success(MessageConstant.ADD + MessageConstant.SUCCESS);
    }

    /**
     * @Description: 更新歌手
     * @Author: Kay
     * @date:   2025/11/21 20:51
     */
    @Override
    @CacheEvict(cacheNames = "artistCache", allEntries = true)
    public Result updateArtist(ArtistUpdateDTO artistUpdateDTO) {

        Long artistId = artistUpdateDTO.getArtistId();

        // 1. 名字被别的 id 占用
        Artist artistByArtistName = artistMapper.selectOne(new LambdaQueryWrapper<Artist>().eq(Artist::getArtistName, artistUpdateDTO.getArtistName()));
        if (artistByArtistName != null && !artistByArtistName.getArtistId().equals(artistId)) {
            return Result.error(MessageConstant.ARTIST + MessageConstant.ALREADY_EXISTS);
        }

        Artist artist = new Artist();
        BeanUtils.copyProperties(artistUpdateDTO, artist);
        if (artistMapper.updateById(artist) == 0) {
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
        }

        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    /**
     * @Description: 更新歌手头像
     * @Author: Kay
     * @date:   2025/11/21 20:55
     */
    @Override
    @CacheEvict(cacheNames = "artistCache", allEntries = true)
    public Result updateArtistAvatar(Long artistId, String avatar) {
        Artist artist = artistMapper.selectById(artistId);
        // 1. 删除已经存在的头像
        String avatarUrl = artist.getAvatar();
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            minioService.deleteFile(avatarUrl);
        }
        // 2. 设置新的头像
        artist.setAvatar(avatar);
        if (artistMapper.updateById(artist) == 0) {
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
        }

        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    /**
     * @Description: 删除歌手
     * @Author: Kay
     * @date:   2025/11/21 20:58
     */
    @Override
    @CacheEvict(cacheNames = "artistCache", allEntries = true)
    public Result deleteArtist(Long artistId) {
        // 1. 查询歌手信息，获取头像 URL
        Artist artist = artistMapper.selectById(artistId);
        if (artist == null) {
            return Result.error(MessageConstant.ARTIST + MessageConstant.NOT_FOUND);
        }
        String avatarUrl = artist.getAvatar();

        // 2. 先删除 MinIO 里的头像文件
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            minioService.deleteFile(avatarUrl);
        }

        // 3. 删除数据库中的歌手信息
        if (artistMapper.deleteById(artistId) == 0) {
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }

        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }

    /**
     * @Description: 批量删除歌手
     * @Author: Kay
     * @date:   2025/11/21 21:00
     */
    @Override
    @CacheEvict(cacheNames = {"artistCache", "songCache"}, allEntries = true)
    public Result deleteArtists(List<Long> artistIds) {

        // 1. 查询歌手信息，获取头像 URL 列表
        List<Artist> artists = artistMapper.selectByIds(artistIds);
        List<String> avatarUrlList = artists.stream()
                .map(Artist::getAvatar)
                .filter(avatarUrl -> avatarUrl != null && !avatarUrl.isEmpty())
                .toList();

        // 2. 先删除 MinIO 里的头像文件
        for (String avatarUrl : avatarUrlList) {
            minioService.deleteFile(avatarUrl);
        }

        // 3. 删除数据库中的歌手信息
        if (artistMapper.deleteByIds(artistIds) == 0) {
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }

        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }

    /**
     * @Description: 获取所有歌手列表
     * @Author: Kay
     * @date:   2025/11/21 21:04
     */
    @Override
    @Cacheable(key = "#artistDTO.pageNum + '-' + #artistDTO.pageSize + '-' + #artistDTO.artistName + '-' + #artistDTO.gender + '-' + #artistDTO.area")
    public Result<PageResult<ArtistVO>> getAllArtists(ArtistDTO artistDTO) {
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

        IPage<Artist> artistPage = artistMapper.selectPage(page, queryWrapper);
        if (artistPage.getRecords().isEmpty()) {
            return Result.success(MessageConstant.DATA_NOT_FOUND, new PageResult<>(0L, null));
        }

        // 转换成 ArtistVO
        List<ArtistVO> artistVOList = artistPage.getRecords().stream()
                .map(artist -> {
                    ArtistVO artistVO = new ArtistVO();
                    BeanUtils.copyProperties(artist, artistVO);
                    return artistVO;
                }).toList();

        return Result.success(new PageResult<>(artistPage.getTotal(), artistVOList));
    }

    /**
     * @Description: 获取随机歌手 , 随机歌手的数量为 10
     * @Author: Kay
     * @date:   2025/11/21 21:08
     */
    @Override
    public Result<List<ArtistVO>> getRandomArtists() {

        LambdaQueryWrapper<Artist> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.last("ORDER BY RAND() LIMIT 10");

        List<Artist> artists = artistMapper.selectList(queryWrapper);
        if (artists.isEmpty()) {
            return Result.success(MessageConstant.DATA_NOT_FOUND, null);
        }

        List<ArtistVO> artistVOList = artists.stream()
                .map(artist -> {
                    ArtistVO artistVO = new ArtistVO();
                    BeanUtils.copyProperties(artist, artistVO);
                    return artistVO;
                }).toList();

        return Result.success(artistVOList);
    }

    /**
     * @Description: 获取歌手详情
     * @Author: Kay
     * @date:   2025/11/21 21:12
     */
    @Override
    @Cacheable(key = "#artistId")
    public Result<ArtistDetailVO> getArtistDetail(Long artistId) {

        ArtistDetailVO artistDetailVO = artistMapper.getArtistDetailById(artistId);

        // 设置默认状态
        List<SongVO> songVOList = artistDetailVO.getSongs();
        songVOList.forEach(songVO -> songVO.setLikeStatus(LikeStatusEnum.DEFAULT.getId()));

        // 将用户点赞的歌曲标记一下
        Long userId = ThreadLocalUtil.getUserId();
        if ( userId != null ) {
            // 获取用户收藏的歌曲
            List<UserFavorite> favoriteSongs = userFavoriteMapper.selectList(new LambdaQueryWrapper<UserFavorite>()
                    .eq(UserFavorite::getUserId, userId)
                    .eq(UserFavorite::getType, 0));
            // 获取用户收藏的歌曲 id
            Set<Long> favoriteSongIds = favoriteSongs.stream()
                    .map(UserFavorite::getSongId)
                    .collect(Collectors.toSet());
            // 检查并更新状态
            for (SongVO songVO : songVOList) {
                if (favoriteSongIds.contains(songVO.getSongId())) {
                    songVO.setLikeStatus(LikeStatusEnum.LIKE.getId());
                }
            }
        }
        // 设置歌曲列表
        artistDetailVO.setSongs(songVOList);

        return Result.success(artistDetailVO);
    }


}
