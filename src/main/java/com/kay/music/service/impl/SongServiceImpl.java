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
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
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
}
