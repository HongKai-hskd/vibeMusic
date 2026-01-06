package com.kay.music.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kay.music.constant.MessageConstant;
import com.kay.music.enumeration.LikeStatusEnum;
import com.kay.music.mapper.ArtistMapper;
import com.kay.music.mapper.SongMapper;
import com.kay.music.mapper.UserFavoriteMapper;
import com.kay.music.pojo.dto.SongAddDTO;
import com.kay.music.pojo.dto.SongAndArtistDTO;
import com.kay.music.pojo.dto.SongDTO;
import com.kay.music.pojo.dto.SongUpdateDTO;
import com.kay.music.pojo.entity.Artist;
import com.kay.music.pojo.entity.Song;
import com.kay.music.pojo.entity.UserFavorite;
import com.kay.music.pojo.vo.SongAdminVO;
import com.kay.music.pojo.vo.SongDetailVO;
import com.kay.music.pojo.vo.SongVO;
import com.kay.music.result.PageResult;
import com.kay.music.result.Result;
import com.kay.music.service.ISongService;
import com.kay.music.service.MinioService;
import com.kay.music.utils.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagException;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.kay.music.constant.RedisConstants;
import com.kay.music.pojo.dto.RedisData;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Kay
 * @date 2025/11/19 23:39
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "songCache")
@Slf4j
public class SongServiceImpl extends ServiceImpl<SongMapper, Song> implements ISongService {
    
    @Resource
    private StringRedisTemplate stringRedisTemplate; // 注入Redis模板
    
    // 线程池，用于异步更新缓存
    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    private final SongMapper songMapper;
    private final UserFavoriteMapper userFavoriteMapper;
    private final MinioService minioService;
    private final ArtistMapper artistMapper;

    /**
     * @Description: 游客版：只需要歌曲列表 + 默认 likeStatus = DEFAULT , 结果对所有“未登录用户”通用，可以全局缓存
     * @Author: Kay
     * @date:   2025/11/20 20:06
     */
    @Cacheable(
            key = "'guest:' + #songDTO.pageNum + '-' + #songDTO.pageSize + '-' + " +
                    "#songDTO.songName + '-' + #songDTO.artistName + '-' + #songDTO.album",
            unless = "#result == null"
    )
    public Result<PageResult<SongVO>> getAllSongsForGuest(SongDTO songDTO) {
                    // 从数据库查询数据
                    // 下面是原来的查询逻辑

        // 使用与缓存穿透类似的方式实现逻辑过期
        String cacheKey = RedisConstants.CACHE_SONG_KEY + "guest:" + 
                songDTO.getPageNum() + ":" + songDTO.getPageSize() + ":" +
                songDTO.getSongName() + ":" + songDTO.getArtistName() + ":" + songDTO.getAlbum();
                
        // 1. 从 Redis 查询缓存
        String json = stringRedisTemplate.opsForValue().get(cacheKey);
        
        // 2. 判断是否存在
        if (StrUtil.isNotBlank(json)) {
            // 2.1 存在，判断是否为空值
            if (json.equals("")) {
                return Result.success(MessageConstant.DATA_NOT_FOUND, new PageResult<>(0L, null));
            }
            
            // 2.2 非空值，需要反序列化
            try {
                RedisData redisData = JSONUtil.toBean(json, RedisData.class);
                PageResult<SongVO> result = JSONUtil.toBean((JSONObject) redisData.getData(), PageResult.class);
                
                // 判断是否过期
                if (redisData.getExpireTime().isAfter(LocalDateTime.now())) {
                    // 未过期，直接返回
                    return Result.success(result);
                }
                
                // 已过期，异步重建缓存
                CACHE_REBUILD_EXECUTOR.submit(() -> {
                    try {
                        // 从数据库重新查询
                        queryFromDatabaseForGuest(songDTO, cacheKey);
                    } catch (Exception e) {
                        log.error("缓存重建异常", e);
                    }
                });
                
                // 返回过期的数据
                return Result.success(result);
            } catch (Exception e) {
                log.error("缓存数据解析异常", e);
            }
        }
        
        // 3. 缓存未命中，从数据库查询
        return queryFromDatabaseForGuest(songDTO, cacheKey);
    }
    
    /**
     * 从数据库查询游客歌曲列表并写入缓存
     */
    private Result<PageResult<SongVO>> queryFromDatabaseForGuest(SongDTO songDTO, String cacheKey) {
        // 1. 查询歌曲列表（分页）
        Page<SongVO> page = new Page<>(songDTO.getPageNum(), songDTO.getPageSize());
        IPage<SongVO> songPage = songMapper.getSongsWithArtist(
                page,
                songDTO.getSongName(),
                songDTO.getArtistName(),
                songDTO.getAlbum()
        );
        
        // 1.2 没有查到，返回空结果（也会被缓存，防止缓存穿透）
        if (songPage.getRecords().isEmpty()) {
            // 将空值写入Redis
            stringRedisTemplate.opsForValue().set(cacheKey, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
            return Result.success(MessageConstant.DATA_NOT_FOUND, new PageResult<>(0L, null));
        }
        
        // 2. 默认全部设置为 未点赞
        List<SongVO> songVOList = songPage.getRecords().stream()
                .peek(songVO -> songVO.setLikeStatus(LikeStatusEnum.DEFAULT.getId()))
                .toList();
        
        PageResult<SongVO> result = new PageResult<>(songPage.getTotal(), songVOList);
        
        // 3. 将数据写入Redis，使用逻辑过期
        setWithLogicalExpire(cacheKey, result);
        
        return Result.success(result);
    }

    /**
     * @Description: 登录用户版：在游客基础上增加“喜欢状态” , 注意：这里的 key 一定要包含 userId，否则会串用户！
     * @Author: Kay
     * @date:   2025/11/20 20:07
     */
    @Override
    @Cacheable(
            key = "'user:' + #userId + '-' + #songDTO.pageNum + '-' + #songDTO.pageSize + '-' + " +
                    "#songDTO.songName + '-' + #songDTO.artistName + '-' + #songDTO.album",
            unless = "#result == null"
    )
    public Result<PageResult<SongVO>> getAllSongsForUser(SongDTO songDTO, Long userId) {
        // 使用逻辑过期机制
        String cacheKey = RedisConstants.CACHE_SONG_KEY + "user:" + userId + ":" + 
                songDTO.getPageNum() + ":" + songDTO.getPageSize() + ":" +
                songDTO.getSongName() + ":" + songDTO.getArtistName() + ":" + songDTO.getAlbum();
                
        // 1. 从 Redis 查询缓存
        String json = stringRedisTemplate.opsForValue().get(cacheKey);
        
        // 2. 判断是否存在
        if (StrUtil.isNotBlank(json)) {
            // 2.1 存在，判断是否为空值
            if (json.equals("")) {
                return Result.success(MessageConstant.DATA_NOT_FOUND, new PageResult<>(0L, null));
            }
            
            // 2.2 非空值，需要反序列化
            try {
                RedisData redisData = JSONUtil.toBean(json, RedisData.class);
                PageResult<SongVO> result = JSONUtil.toBean((JSONObject) redisData.getData(), PageResult.class);
                
                // 判断是否过期
                if (redisData.getExpireTime().isAfter(LocalDateTime.now())) {
                    // 未过期，直接返回
                    return Result.success(result);
                }
                
                // 已过期，异步重建缓存
                CACHE_REBUILD_EXECUTOR.submit(() -> {
                    try {
                        // 从数据库重新查询
                        queryFromDatabaseForUser(songDTO, userId, cacheKey);
                    } catch (Exception e) {
                        log.error("缓存重建异常", e);
                    }
                });
                
                // 返回过期的数据
                return Result.success(result);
            } catch (Exception e) {
                log.error("缓存数据解析异常", e);
            }
        }
        
        // 3. 缓存未命中，从数据库查询
        return queryFromDatabaseForUser(songDTO, userId, cacheKey);
    }
    
    /**
     * 从数据库查询用户歌曲列表并写入缓存
     */
    private Result<PageResult<SongVO>> queryFromDatabaseForUser(SongDTO songDTO, Long userId, String cacheKey) {
                    // 从数据库查询数据
                    // 下面是原来的查询逻辑

        Page<SongVO> page = new Page<>(songDTO.getPageNum(), songDTO.getPageSize());
        IPage<SongVO> songPage = songMapper.getSongsWithArtist(
                page,
                songDTO.getSongName(),
                songDTO.getArtistName(),
                songDTO.getAlbum()
        );

        if (songPage.getRecords().isEmpty()) {
            // 没有查到结果，返回空结果（也会被缓存，防止缓存穿透）
            stringRedisTemplate.opsForValue().set(cacheKey, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
            return Result.success(MessageConstant.DATA_NOT_FOUND, new PageResult<>(0L, null));
        }
        
        // 2. 默认全部设置为 未点赞
        List<SongVO> songVOList = songPage.getRecords().stream()
                .peek(songVO -> songVO.setLikeStatus(LikeStatusEnum.DEFAULT.getId()))
                .toList();
                
        // 3. 获取用户收藏的歌曲
        List<UserFavorite> favoriteSongs = userFavoriteMapper.selectList(
                new LambdaQueryWrapper<UserFavorite>()
                        .eq(UserFavorite::getUserId, userId)
                        .eq(UserFavorite::getType, 0)
        );
        
        // 4. 找到自己点赞了的故去id
        Set<Long> favoriteSongIds = favoriteSongs.stream()
                .map(UserFavorite::getSongId)
                .collect(Collectors.toSet());

        for (SongVO songVO : songVOList) {
            if (favoriteSongIds.contains(songVO.getSongId())) {
                songVO.setLikeStatus(LikeStatusEnum.LIKE.getId());
            }
        }
        
        PageResult<SongVO> result = new PageResult<>(songPage.getTotal(), songVOList);
        
        // 5. 将数据写入Redis，使用逻辑过期
        setWithLogicalExpire(cacheKey, result);
        
        return Result.success(result);
    }

    /**
     * @Description: 获取推荐歌曲, 推荐歌曲的数量为 20
     * @Author: Kay
     * @date:   2025/11/20 23:32
     */
    // TODO: 推荐方式需要修改，当前为随机推荐，后续可以基于用户行为、协同过滤等方式实现个性化推荐
    @Override
    @Cacheable(key = "'recommended'", unless = "#result == null")
    public Result<List<SongVO>> getRecommendedSongs() {
        // 使用逻辑过期方式从缓存获取
        String cacheKey = RedisConstants.CACHE_SONG_KEY + "recommended";
        
        // 1. 从 Redis 查询缓存
        String json = stringRedisTemplate.opsForValue().get(cacheKey);
        
        // 2. 判断是否存在
        if (StrUtil.isNotBlank(json)) {
            // 2.1 存在，判断是否为空值
            if (json.equals("")) {
                return Result.success(MessageConstant.DATA_NOT_FOUND, null);
            }
            
            // 2.2 非空值，需要反序列化
            try {
                RedisData redisData = JSONUtil.toBean(json, RedisData.class);
                List<SongVO> result = JSONUtil.toBean((JSONObject) redisData.getData(), List.class);
                
                // 判断是否过期
                if (redisData.getExpireTime().isAfter(LocalDateTime.now())) {
                    // 未过期，直接返回
                    return Result.success(result);
                }
                
                // 已过期，异步重建缓存
                CACHE_REBUILD_EXECUTOR.submit(() -> {
                    try {
                        // 从数据库重新查询
                        queryRecommendedSongsFromDb(cacheKey);
                    } catch (Exception e) {
                        log.error("缓存重建异常", e);
                    }
                });
                
                // 返回过期的数据
                return Result.success(result);
            } catch (Exception e) {
                log.error("缓存数据解析异常", e);
            }
        }
        
        // 3. 缓存未命中，从数据库查询
        return queryRecommendedSongsFromDb(cacheKey);
    }
    
    /**
     * 从数据库查询推荐歌曲并写入缓存
     */
    private Result<List<SongVO>> queryRecommendedSongsFromDb(String cacheKey) {
        // 目前简化为随机推荐
        List<SongVO> recommendedSongs = songMapper.getRandomSongsWithArtist();
        if (recommendedSongs == null || recommendedSongs.isEmpty()) {
            // 将空值写入Redis
            stringRedisTemplate.opsForValue().set(cacheKey, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
            return Result.success(MessageConstant.DATA_NOT_FOUND, null);
        }
        
        // 将结果写入Redis，使用逻辑过期
        setWithLogicalExpire(cacheKey, recommendedSongs);
        
        return Result.success(recommendedSongs);
    }

    /**
     * @Description: 获取歌曲详情，如果用户登录了，还需要关注是否点赞
     * @Author: Kay
     * @date:   2025/11/21 10:34
     */

    @Override
    // 不再使用Spring的缓存注解，而是手动实现逻辑过期机制
    public Result<SongDetailVO> getSongDetail(Long songId, HttpServletRequest request) {
        String key = RedisConstants.CACHE_SONG_KEY + songId;
        
        // 1. 从 Redis 查询缓存
        String json = stringRedisTemplate.opsForValue().get(key);
        
        // 2. 判断是否存在
        if (StrUtil.isBlank(json)) {
            // 3. 不存在，直接从数据库查询
            return queryWithPassThrough(songId, request);
        }
        
        // 4. 存在，判断是否为空值
        if (json.equals("")) {
            // 返回错误信息
            return Result.error(MessageConstant.SONG + MessageConstant.NOT_FOUND, null);
        }
        
        // 5. 并非空值，需要反序列化为对象
        RedisData redisData = JSONUtil.toBean(json, RedisData.class);
        SongDetailVO songDetailVO = JSONUtil.toBean((JSONObject) redisData.getData(), SongDetailVO.class);
        LocalDateTime expireTime = redisData.getExpireTime();
        
        // 6. 判断是否过期
        if (expireTime.isAfter(LocalDateTime.now())) {
            // 6.1 未过期，直接返回
            return Result.success(songDetailVO);
        }
        
        // 7. 已过期，需要缓存重建
        // 7.1 获取互斥锁
        String lockKey = RedisConstants.LOCK_KEY + key;
        boolean isLock = tryLock(lockKey);
        
        // 7.2 判断是否获取锁成功
        if (isLock) {
            // 7.3 成功，开启独立线程，实现缓存重建
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    // 查询数据库
                    SongDetailVO newSongDetailVO = getFromDatabase(songId);
                    // 重建缓存
                    setWithLogicalExpire(key, newSongDetailVO);
                } catch (Exception e) {
                    log.error("缓存重建异常", e);
                } finally {
                    // 释放锁
                    unlock(lockKey);
                }
            });
        }

        // 8. 返回缓存中的数据（包括过期数据）
        // 如果用户登录了，还需要设置喜欢状态
        Long userId = ThreadLocalUtil.getUserId();
        if (userId != null) {
            // 获取用户收藏的歌曲
            UserFavorite favoriteSong = userFavoriteMapper.selectOne(new LambdaQueryWrapper<UserFavorite>()
                    .eq(UserFavorite::getUserId, userId)
                    .eq(UserFavorite::getType, 0)
                    .eq(UserFavorite::getSongId, songId));
            if (favoriteSong != null) {
                songDetailVO.setLikeStatus(LikeStatusEnum.LIKE.getId());
            }
        }
        
        return Result.success(songDetailVO);
    }
    
    /**
     * 使用缓存穿透解决方案查询数据库
     */
    private Result<SongDetailVO> queryWithPassThrough(Long songId, HttpServletRequest request) {
        String key = RedisConstants.CACHE_SONG_KEY + songId;
        // 从数据库查询
        SongDetailVO songDetailVO = getFromDatabase(songId);
        
        // 如果数据库中也不存在
        if (songDetailVO == null) {
            // 将空值写入Redis，防止缓存穿透
            stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
            return Result.error(MessageConstant.SONG + MessageConstant.NOT_FOUND, null);
        }
        
        // 添加用户收藏状态
        Long userId = ThreadLocalUtil.getUserId();
        if (userId != null) {
            UserFavorite favoriteSong = userFavoriteMapper.selectOne(new LambdaQueryWrapper<UserFavorite>()
                    .eq(UserFavorite::getUserId, userId)
                    .eq(UserFavorite::getType, 0)
                    .eq(UserFavorite::getSongId, songId));
            if (favoriteSong != null) {
                songDetailVO.setLikeStatus(LikeStatusEnum.LIKE.getId());
            }
        }
        
        // 将数据写入Redis，使用逻辑过期
        setWithLogicalExpire(key, songDetailVO);
        
        return Result.success(songDetailVO);
    }

    /**
     * @Description: 获取所有歌曲的数量
     * @Author: Kay
     * @date:   2025/11/21 21:31
     */
    @Override
    public Result<Long> getAllSongsCount() {
        return Result.success(songMapper.selectCount(null));
    }

    /**
     * @Description: 获取歌手的所有歌曲
     * @Author: Kay
     * @date:   2025/11/21 21:36
     */
    @Override
    @Cacheable(key = "'artist-songs:' + #songDTO.pageNum + '-' + #songDTO.pageSize + '-' + #songDTO.songName + '-' + #songDTO.album + '-' + #songDTO.artistId", unless = "#result == null")
    public Result<PageResult<SongAdminVO>> getAllSongsByArtist(SongAndArtistDTO songDTO) {
        // 使用逻辑过期方式从缓存获取
        String cacheKey = RedisConstants.CACHE_SONG_KEY + "artist:" + songDTO.getArtistId() + ":" + 
                songDTO.getPageNum() + ":" + songDTO.getPageSize() + ":" +
                songDTO.getSongName() + ":" + songDTO.getAlbum();
                
        // 1. 从 Redis 查询缓存
        String json = stringRedisTemplate.opsForValue().get(cacheKey);
        
        // 2. 判断是否存在
        if (StrUtil.isNotBlank(json)) {
            // 2.1 存在，判断是否为空值
            if (json.equals("")) {
                return Result.success(MessageConstant.DATA_NOT_FOUND, new PageResult<>(0L, null));
            }
            
            // 2.2 非空值，需要反序列化
            try {
                RedisData redisData = JSONUtil.toBean(json, RedisData.class);
                PageResult<SongAdminVO> result = JSONUtil.toBean((JSONObject) redisData.getData(), PageResult.class);
                
                // 判断是否过期
                if (redisData.getExpireTime().isAfter(LocalDateTime.now())) {
                    // 未过期，直接返回
                    return Result.success(result);
                }
                
                // 已过期，异步重建缓存
                CACHE_REBUILD_EXECUTOR.submit(() -> {
                    try {
                        // 从数据库重新查询
                        querySongsByArtistFromDb(songDTO, cacheKey);
                    } catch (Exception e) {
                        log.error("缓存重建异常", e);
                    }
                });
                
                // 返回过期的数据
                return Result.success(result);
            } catch (Exception e) {
                log.error("缓存数据解析异常", e);
            }
        }
        
        // 3. 缓存未命中，从数据库查询
        return querySongsByArtistFromDb(songDTO, cacheKey);
    }
    
    /**
     * 从数据库查询艺术家歌曲并写入缓存
     */
    private Result<PageResult<SongAdminVO>> querySongsByArtistFromDb(SongAndArtistDTO songDTO, String cacheKey) {
        // 分页查询
        Page<SongAdminVO> page = new Page<>(songDTO.getPageNum(), songDTO.getPageSize());
        IPage<SongAdminVO> songPage = songMapper.getSongsWithArtistName(page, songDTO.getArtistId(), songDTO.getSongName(), songDTO.getAlbum());

        if (songPage.getRecords().isEmpty()) {
            // 缓存空结果，防止缓存穿透
            stringRedisTemplate.opsForValue().set(cacheKey, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
            return Result.success(MessageConstant.DATA_NOT_FOUND, new PageResult<>(0L, null));
        }
        
        PageResult<SongAdminVO> result = new PageResult<>(songPage.getTotal(), songPage.getRecords());
        
        // 将结果存入Redis，使用逻辑过期
        setWithLogicalExpire(cacheKey, result);
        
        return Result.success(result);
    }

    /**
     * @Description: 添加歌曲信息
     * @Author: Kay
     * @date:   2025/11/21 21:41
     */
    @Override
    @CacheEvict(cacheNames = "songCache", allEntries = true)
    public Result addSong(SongAddDTO songAddDTO) {
        Song song = new Song();
        BeanUtils.copyProperties(songAddDTO, song);

        // 插入歌曲记录
        if (songMapper.insert(song) == 0) {
            return Result.error(MessageConstant.ADD + MessageConstant.FAILED);
        }
        // 获取刚插入的歌曲记录
        Song songInDB = songMapper.selectOne(new LambdaQueryWrapper<Song>()
                .eq(Song::getArtistId, songAddDTO.getArtistId())
                .eq(Song::getSongName, songAddDTO.getSongName())
                .eq(Song::getAlbum, songAddDTO.getAlbum())
                .orderByDesc(Song::getSongId)
                .last("LIMIT 1"));

        if (songInDB == null) {
            return Result.error(MessageConstant.SONG + MessageConstant.NOT_FOUND);
        }

        return Result.success(MessageConstant.ADD + MessageConstant.SUCCESS);
    }

    /**
     * @Description: 更新歌曲信息
     * @Author: Kay
     * @date:   2025/11/21 21:48
     */
    @Override
    @CacheEvict(cacheNames = "songCache", allEntries = true)
    public Result updateSong(SongUpdateDTO songUpdateDTO) {

        // 查询数据库中是否存在该歌曲
        Song songInDB = songMapper.selectById(songUpdateDTO.getSongId());
        if (songInDB == null) {
            return Result.error(MessageConstant.SONG + MessageConstant.NOT_FOUND);
        }

        // 更新歌曲基本信息
        Song song = new Song();
        BeanUtils.copyProperties(songUpdateDTO, song);
        if (songMapper.updateById(song) == 0) {
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
        }

        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    /**
     * @Description: 更新歌曲封面
     * @Author: Kay
     * @date:   2025/11/22 15:01
     */
    @Override
    @CacheEvict(cacheNames = "songCache", allEntries = true)
    public Result updateSongCover(Long songId, String coverUrl) {
        Song song = songMapper.selectById(songId);
        String cover = song.getCoverUrl();
        if (cover != null && !cover.isEmpty()) {
            minioService.deleteFile(cover);
        }

        song.setCoverUrl(coverUrl);
        if (songMapper.updateById(song) == 0) {
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
        }

        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    /**
     * @Description: 更新歌曲音频
     * @Author: Kay
     * @date:   2025/11/22 15:02
     */
    @Override
    @CacheEvict(cacheNames = "songCache", allEntries = true)
    public Result updateSongAudio(Long songId, String audioUrl, String duration) {
        Song song = songMapper.selectById(songId);
        String audio = song.getAudioUrl();
        if (audio != null && !audio.isEmpty()) {
            minioService.deleteFile(audio);
        }

        song.setAudioUrl(audioUrl).setDuration(duration);
        if (songMapper.updateById(song) == 0) {
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
        }

        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    /**
     * @Description: 删除歌曲
     * @Author: Kay
     * @date:   2025/11/22 15:04
     */
    @Override
    @CacheEvict(cacheNames = "songCache", allEntries = true)
    public Result deleteSong(Long songId) {
        Song song = songMapper.selectById(songId);
        if (song == null) {
            return Result.error(MessageConstant.SONG + MessageConstant.NOT_FOUND);
        }
        String cover = song.getCoverUrl();
        String audio = song.getAudioUrl();

        if (cover != null && !cover.isEmpty()) {
            minioService.deleteFile(cover);
        }
        if (audio != null && !audio.isEmpty()) {
            minioService.deleteFile(audio);
        }

        if (songMapper.deleteById(songId) == 0) {
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }

        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }

    /**
     * @Description: 批量删除歌曲
     * @Author: Kay
     * @date:   2025/11/22 15:05
     */
    @Override
    @CacheEvict(cacheNames = "songCache", allEntries = true)
    public Result deleteSongs(List<Long> songIds) {
        // 1. 查询歌曲信息，获取歌曲封面 URL 列表
        List<Song> songs = songMapper.selectByIds(songIds);
        List<String> coverUrlList = songs.stream()
                .map(Song::getCoverUrl)
                .filter(coverUrl -> coverUrl != null && !coverUrl.isEmpty())
                .toList();
        List<String> audioUrlList = songs.stream()
                .map(Song::getAudioUrl)
                .filter(audioUrl -> audioUrl != null && !audioUrl.isEmpty())
                .toList();

        // 2. 先删除 MinIO 里的歌曲封面和音频文件
        for (String coverUrl : coverUrlList) {
            minioService.deleteFile(coverUrl);
        }
        for (String audioUrl : audioUrlList) {
            minioService.deleteFile(audioUrl);
        }

        // 3. 删除数据库中的歌曲信息
        if (songMapper.deleteByIds(songIds) == 0) {
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }

        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }

    @Override
    @CacheEvict(cacheNames = {"songCache", "artistCache"}, allEntries = true)
    public Result addSongByFile(MultipartFile audio) {
        if (audio.isEmpty()) {
            return Result.error("上传的文件为空");
        }
        addSongByFileFunction(audio);
        return Result.success("文件处理成功，信息已输出");
    }

    @Override
    @CacheEvict(cacheNames = {"songCache", "artistCache"}, allEntries = true)
    public Result batchAddSongByFile(MultipartFile[] audios) {
        for (MultipartFile audio : audios) {
            addSongByFileFunction(audio);
        }
        // 返回操作成功的结果
        return Result.success("文件处理成功，信息已输出");
    }

    private void addSongByFileFunction(MultipartFile audio){
        try {

            String audioUrl = minioService.uploadFile(audio, "songs");

            // 获取上传文件的原始文件名
            String originalFilename = audio.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                return;
                // return Result.error("文件名为空");
            }

            // 获取文件的扩展名，确保是支持的音频格式（比如 mp3, flac, wav）
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            if (!extension.equalsIgnoreCase(".mp3") && !extension.equalsIgnoreCase(".flac") && !extension.equalsIgnoreCase(".wav")) {
                // return Result.error("不支持的文件格式");
                return;
            }

            // 1. 创建临时文件并添加正确的扩展名
            File tempFile = File.createTempFile("temp_audio", extension);

            // 2. 将上传的文件内容复制到临时文件
            audio.transferTo(tempFile);

            // 3. 使用 jaudiotagger 读取音频文件标签
            AudioFile audioFile = AudioFileIO.read(tempFile);
            org.jaudiotagger.tag.Tag tag = audioFile.getTag();

            // 4. 读取 ID3 标签（标题、艺术家、专辑等）
            if (tag != null) {
                String title = tag.getFirst(FieldKey.TITLE);
                String artistName = tag.getFirst(FieldKey.ARTIST);
                String album = tag.getFirst(FieldKey.ALBUM);
                String trackStr = tag.getFirst(FieldKey.TRACK);
                String yearStr = tag.getFirst(FieldKey.YEAR);

                // 如果 title 为空，则使用文件名去除扩展名作为标题
                if (title == null || title.isEmpty()) {
                    title = originalFilename.substring(0, originalFilename.lastIndexOf("."));
                }

                // 5. 使用 log 输出标签信息
                log.info("Title: {}", title != null ? title : "N/A");
                log.info("Artist: {}", artistName != null ? artistName : "N/A");
                log.info("Album: {}", album != null ? album : "N/A");
                log.info("Track: {}", trackStr != null ? trackStr : "N/A");
                log.info("Year: {}", yearStr != null ? yearStr : "N/A");

                // 6. 获取歌曲时长（秒数）
                long durationMillis = audioFile.getAudioHeader().getTrackLength() * 1000L;
                long minutes = durationMillis / 60000;
                long seconds = (durationMillis % 60000) / 1000;
                log.info("Duration: {} minutes {} seconds", minutes, seconds);


                // 1. 查找有无歌手信息 ， 获取 歌手 id
                Artist artist = artistMapper.selectOne(new LambdaQueryWrapper<Artist>().eq(Artist::getArtistName, artistName));
                // 1.1 有则 获取 id
                Long artistId;
                if ( artist != null ) {
                    artistId = artist.getArtistId();
                } else {
                    // 1.2 无则 手动插入 并获取 id
                    Artist newArtist = new Artist().setArtistName(artistName);
                    artistMapper.insert(newArtist);
                    artistId = newArtist.getArtistId();
                }

                // 2. 组装已有信息
                Song song = new Song()
                        .setSongName(title)
                        .setArtistId(artistId)
                        .setAlbum(album)
                        .setAudioUrl(audioUrl)
                        .setReleaseTime(parseYearToLocalDate(yearStr)) // 将年份转换为 LocalDate
                        .setDuration(formatDuration(durationMillis)); // 将时长格式化为字符串

                // 3. 插入信息
                songMapper.insert(song);



            } else {
                log.info("No ID3 tags found in the file.");
            }

            // 删除临时文件
            tempFile.delete();


        } catch (IOException | org.jaudiotagger.audio.exceptions.CannotReadException e) {
            e.printStackTrace();
        } catch (TagException | InvalidAudioFrameException | ReadOnlyFileException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从数据库获取歌曲详情
     * 
     * @param id 歌曲ID
     * @return 歌曲详情对象
     */
    private SongDetailVO getFromDatabase(Long id) {
        return songMapper.getSongDetailById(id);
    }
    
    /**
     * 将数据存入Redis，设置逻辑过期时间
     * 
     * @param key 缓存键
     * @param value 缓存值
     */
    private <T> void setWithLogicalExpire(String key, T value) {
        // 设置逻辑过期
        RedisData<T> redisData = new RedisData<>();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusMinutes(RedisConstants.CACHE_SONG_TTL));
        
        // 写入Redis，不设置RedisTTL过期时间
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }
    
    /**
     * 尝试获取锁
     * 
     * @param key 锁的键
     * @return 是否获取成功
     */
    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", RedisConstants.LOCK_TTL, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(flag);
    }
    
    /**
     * 释放锁
     * 
     * @param key 锁的键
     */
    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }
    
    // 将年份字符串转换为 LocalDate，若无效则返回当前日期
    private LocalDate parseYearToLocalDate(String yearStr) {
        try {
            if (yearStr != null && !yearStr.isEmpty()) {
                return LocalDate.parse(yearStr + "-01-01", DateTimeFormatter.ISO_DATE); // 使用默认的01-01作为日期
            }
        } catch (DateTimeParseException e) {
            // 如果解析失败，返回当前日期（可以根据需求修改为其他默认值）
            return LocalDate.now();
        }
        return LocalDate.now(); // 如果 yearStr 为 null 或空，则返回当前日期
    }

    // 将时长（毫秒）转换为字符串格式，保留两位小数
    private String formatDuration(long durationMillis) {
        double durationSeconds = durationMillis / 1000.0; // 转换为秒
        return String.format("%.2f", durationSeconds); // 格式化为字符串
    }

}
