package com.kay.music.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kay.music.constant.MessageConstant;
import com.kay.music.enumeration.LikeStatusEnum;
import com.kay.music.mapper.SongMapper;
import com.kay.music.mapper.UserFavoriteMapper;
import com.kay.music.pojo.dto.SongDTO;
import com.kay.music.pojo.entity.Song;
import com.kay.music.pojo.entity.UserFavorite;
import com.kay.music.pojo.vo.SongVO;
import com.kay.music.result.PageResult;
import com.kay.music.result.Result;
import com.kay.music.service.ISongService;
import com.kay.music.utils.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Kay
 * @date 2025/11/19 23:39
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "songCache")
public class SongServiceImpl extends ServiceImpl<SongMapper, Song> implements ISongService {

    private final SongMapper songMapper;
    private final UserFavoriteMapper userFavoriteMapper;
    private final RedisTemplate redisTemplate;

    /**
     * @Description: 游客版：只需要歌曲列表 + 默认 likeStatus = DEFAULT , 结果对所有“未登录用户”通用，可以全局缓存
     * @Author: Kay
     * @date:   2025/11/20 20:06
     */
    @Cacheable(
            key = "#songDTO.pageNum + '-' + #songDTO.pageSize + '-' + " +
                    "#songDTO.songName + '-' + #songDTO.artistName + '-' + #songDTO.album"
    )
    public Result<PageResult<SongVO>> getAllSongsForGuest(SongDTO songDTO) {

        // 1. 查询歌曲列表（分页）
        //Page = 分页请求参数（包含当前页和每页数量）
        //IPage = 分页后的完整结果（包含列表 + 总条数 + 分页信息）
        Page<SongVO> page = new Page<>(songDTO.getPageNum(), songDTO.getPageSize());
        IPage<SongVO> songPage = songMapper.getSongsWithArtist(
                page,
                songDTO.getSongName(),
                songDTO.getArtistName(),
                songDTO.getAlbum()
        );
        // 1.2 没有查到
        if (songPage.getRecords().isEmpty()) {
            return Result.success(MessageConstant.DATA_NOT_FOUND, new PageResult<>(0L, null));
        }
        // 2. 默认全部设置为 未点赞
        List<SongVO> songVOList = songPage.getRecords().stream()
                .peek(songVO -> songVO.setLikeStatus(LikeStatusEnum.DEFAULT.getId()))
                .toList();

        return Result.success(new PageResult<>(songPage.getTotal(), songVOList));
    }

    /**
     * @Description: 登录用户版：在游客基础上增加“喜欢状态” , 注意：这里的 key 一定要包含 userId，否则会串用户！
     * @Author: Kay
     * @date:   2025/11/20 20:07
     */
    @Cacheable(
            key = "#userId + '-' + #songDTO.pageNum + '-' + #songDTO.pageSize + '-' + " +
                    "#songDTO.songName + '-' + #songDTO.artistName + '-' + #songDTO.album"
    )
    public Result<PageResult<SongVO>> getAllSongsForUser(SongDTO songDTO, Long userId) {

        Page<SongVO> page = new Page<>(songDTO.getPageNum(), songDTO.getPageSize());
        IPage<SongVO> songPage = songMapper.getSongsWithArtist(
                page,
                songDTO.getSongName(),
                songDTO.getArtistName(),
                songDTO.getAlbum()
        );

        if (songPage.getRecords().isEmpty()) {
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

        return Result.success(new PageResult<>(songPage.getTotal(), songVOList));
    }

    /**
     * @Description: 获取推荐歌曲, 推荐歌曲的数量为 20
     * @Author: Kay
     * @date:   2025/11/20 23:32
     */
    // TODO：当前的 sortedStyleIds 只是根据风格出现频率排序，
    //       但实际 SQL 查询只使用了 IN (...)，不会根据排序影响候选歌曲权重。
    //       后续需要将“风格权重”真正应用到推荐逻辑中，例如：
    //       1) 按权重进行加权随机推荐，让风格出现概率与用户偏好一致。
    //       2) 或在 SQL 中使用 CASE/FIELD 手动排序，让高权重风格优先返回。
    //       3) 或分批按风格查询，按比例分配歌曲数量。
    //       当前逻辑仍然能跑，但没有真正体现个性化权重，需要之后优化。

    @Override
    public Result<List<SongVO>> getRecommendedSongs() {

        Long userId = ThreadLocalUtil.getUserId();
        // 1. 用户未登录，返回随机歌曲列表
        if ( userId == null ) {
            return Result.success(songMapper.getRandomSongsWithArtist());
        }
        // 2.1 查询用户收藏的歌曲 ID
        List<Long> favoriteSongIds = userFavoriteMapper.getFavoriteSongIdsByUserId(userId);
        // 2.2 如果查不到，则随机返回
        if (favoriteSongIds.isEmpty()) {
            return Result.success(songMapper.getRandomSongsWithArtist());
        }

        // 3. 根据用户收藏歌曲，查询用户收藏的歌曲风格，并统计频率
        List<Long> favoriteStyleIds = songMapper.getFavoriteSongStyles(favoriteSongIds);
        Map<Long, Long> styleFrequency = favoriteStyleIds.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        // 4. 按风格出现次数降序排序
        List<Long> sortedStyleIds = styleFrequency.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 5. 从 Redis 获取缓存的推荐列表 ， range 0 , -1 :  “从头到尾所有元素”
        String redisKey = "recommended_songs:" + userId;
        List<SongVO> cachedSongs = redisTemplate.opsForList().range(redisKey, 0, -1);

        // 如果 Redis 没有缓存，则查询数据库并缓存
        if (cachedSongs == null || cachedSongs.isEmpty()) {
            // 根据排序后的风格推荐歌曲（排除已收藏歌曲）
            cachedSongs = songMapper.getRecommendedSongsByStyles(sortedStyleIds, favoriteSongIds, 80);
            redisTemplate.opsForList().rightPushAll(redisKey, cachedSongs);
            redisTemplate.expire(redisKey, 30, TimeUnit.MINUTES); // 设置过期时间 30 分钟
        }

        // 随机选取 20 首
        Collections.shuffle(cachedSongs);
        List<SongVO> recommendedSongs = cachedSongs.subList(0, Math.min(20, cachedSongs.size()));

        // 如果推荐的歌曲不足 20 首，则用随机歌曲填充
        if (recommendedSongs.size() < 20) {
            List<SongVO> randomSongs = songMapper.getRandomSongsWithArtist();
            Set<Long> addedSongIds = recommendedSongs.stream().map(SongVO::getSongId).collect(Collectors.toSet());
            for (SongVO song : randomSongs) {
                if (recommendedSongs.size() >= 20) break;
                if (!addedSongIds.contains(song.getSongId())) {
                    recommendedSongs.add(song);
                }
            }
        }

        return Result.success(recommendedSongs);
    }






}
