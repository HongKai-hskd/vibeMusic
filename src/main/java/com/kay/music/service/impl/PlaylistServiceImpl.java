package com.kay.music.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kay.music.constant.MessageConstant;
import com.kay.music.enumeration.LikeStatusEnum;
import com.kay.music.mapper.PlaylistMapper;
import com.kay.music.mapper.UserFavoriteMapper;
import com.kay.music.pojo.dto.PlaylistAddDTO;
import com.kay.music.pojo.dto.PlaylistDTO;
import com.kay.music.pojo.dto.PlaylistUpdateDTO;
import com.kay.music.pojo.entity.Playlist;
import com.kay.music.pojo.entity.UserFavorite;
import com.kay.music.pojo.vo.PlaylistDetailVO;
import com.kay.music.pojo.vo.PlaylistVO;
import com.kay.music.pojo.vo.SongVO;
import com.kay.music.result.PageResult;
import com.kay.music.result.Result;
import com.kay.music.service.IPlaylistService;
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
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Kay
 * @date 2025/11/22 15:10
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "playlistCache")
public class PlaylistServiceImpl extends ServiceImpl<PlaylistMapper, Playlist> implements IPlaylistService {

    private final PlaylistMapper playlistMapper;
    private final MinioService minioService;
    private final ThreadLocalUtil threadLocalUtil;
    private final UserFavoriteMapper userFavoriteMapper;

    /**
     * @Description: 获取所有歌单数量
     * @Author: Kay
     * @date:   2025/11/22 15:11
     */
    @Override
    public Result<Long> getAllPlaylistsCount(String style) {
        QueryWrapper<Playlist> queryWrapper = new QueryWrapper<>();
        if (style != null) {
            queryWrapper.eq("style", style);
        }

        return Result.success(playlistMapper.selectCount(queryWrapper));
    }
    
    /**
     * @Description: 获取所有歌单信息
     * @Author: Kay
     * @date:   2025/11/22 15:15
     */
    @Override
    @Cacheable(key = "#playlistDTO.pageNum + '-' + #playlistDTO.pageSize + '-' + #playlistDTO.title + '-' + #playlistDTO.style + '-admin'")
    public Result<PageResult<Playlist>> getAllPlaylistsInfo(PlaylistDTO playlistDTO) {
        // 分页查询
        Page<Playlist> page = new Page<>(playlistDTO.getPageNum(), playlistDTO.getPageSize());
        QueryWrapper<Playlist> queryWrapper = new QueryWrapper<>();
        // 根据 playlistDTO 的条件构建查询条件
        if (playlistDTO.getTitle() != null) {
            queryWrapper.like("title", playlistDTO.getTitle());
        }
        if (playlistDTO.getStyle() != null) {
            queryWrapper.eq("style", playlistDTO.getStyle());
        }
        // 倒序排序
        queryWrapper.orderByDesc("id");

        IPage<Playlist> playlistPage = playlistMapper.selectPage(page, queryWrapper);
        if (playlistPage.getRecords().isEmpty()) {
            return Result.success(MessageConstant.DATA_NOT_FOUND, new PageResult<>(0L, null));
        }

        return Result.success(new PageResult<>(playlistPage.getTotal(), playlistPage.getRecords()));
    }

    /**
     * @Description: 添加歌单
     * @Author: Kay
     * @date:   2025/11/22 15:18
     */
    @Override
    @CacheEvict(cacheNames = "playlistCache", allEntries = true)
    public Result addPlaylist(PlaylistAddDTO playlistAddDTOO) {
        QueryWrapper<Playlist> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("title", playlistAddDTOO.getTitle());
        if (playlistMapper.selectCount(queryWrapper) > 0) {
            return Result.error(MessageConstant.PLAYLIST + MessageConstant.ALREADY_EXISTS);
        }

        Playlist playlist = new Playlist();
        BeanUtils.copyProperties(playlistAddDTOO, playlist);
        playlistMapper.insert(playlist);

        return Result.success(MessageConstant.ADD + MessageConstant.SUCCESS);
    }

    /**
     * @Description: 更新歌单
     * @Author: Kay
     * @date:   2025/11/22 15:20
     */
    @Override
    @CacheEvict(cacheNames = "playlistCache", allEntries = true)
    public Result updatePlaylist(PlaylistUpdateDTO playlistUpdateDTO) {
        Long playlistId = playlistUpdateDTO.getPlaylistId();

        Playlist playlistByTitle = playlistMapper.selectOne(new QueryWrapper<Playlist>().eq("title", playlistUpdateDTO.getTitle()));
        if (playlistByTitle != null && !playlistByTitle.getPlaylistId().equals(playlistId)) {
            return Result.error(MessageConstant.PLAYLIST + MessageConstant.ALREADY_EXISTS);
        }

        Playlist playlist = new Playlist();
        BeanUtils.copyProperties(playlistUpdateDTO, playlist);
        if (playlistMapper.updateById(playlist) == 0) {
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
        }

        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    /**
     * @Description:  更新歌单封面
     * @Author: Kay
     * @date:   2025/11/22 15:22
     */
    @Override
    @CacheEvict(cacheNames = "playlistCache", allEntries = true)
    public Result updatePlaylistCover(Long playlistId, String coverUrl) {
        Playlist playlist = playlistMapper.selectById(playlistId);
        String cover = playlist.getCoverUrl();
        if (cover != null && !cover.isEmpty()) {
            minioService.deleteFile(cover);
        }

        playlist.setCoverUrl(coverUrl);
        if (playlistMapper.updateById(playlist) == 0) {
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
        }

        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }


    @Override
    @CacheEvict(cacheNames = "playlistCache", allEntries = true)
    public Result deletePlaylist(Long playlistId) {
        // 1. 查询歌单信息，获取封面 URL
        Playlist playlist = playlistMapper.selectById(playlistId);
        if (playlist == null) {
            return Result.error(MessageConstant.PLAYLIST + MessageConstant.NOT_FOUND);
        }
        String coverUrl = playlist.getCoverUrl();

        // 2. 先删除 MinIO 里的封面文件
        if (coverUrl != null && !coverUrl.isEmpty()) {
            minioService.deleteFile(coverUrl);
        }

        // 3. 删除数据库中的歌单信息
        if (playlistMapper.deleteById(playlistId) == 0) {
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }

        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }

    @Override
    @CacheEvict(cacheNames = "playlistCache", allEntries = true)
    public Result deletePlaylists(List<Long> playlistIds) {
        List<Playlist> playlists = playlistMapper.selectBatchIds(playlistIds);
        List<String> coverUrlList = playlists.stream()
                .map(Playlist::getCoverUrl)
                .filter(coverUrl -> coverUrl != null && !coverUrl.isEmpty())
                .toList();

        // 2. 先删除 MinIO 里的封面文件
        for (String coverUrl : coverUrlList) {
            minioService.deleteFile(coverUrl);
        }

        // 3. 删除数据库中的歌单信息
        if (playlistMapper.deleteBatchIds(playlistIds) == 0) {
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }

        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }

    /**
     * 获取所有歌单
     *
     * @param playlistDTO playlistDTO
     * @return 歌单列表
     */
    @Override
    @Cacheable(key = "#playlistDTO.pageNum + '-' + #playlistDTO.pageSize + '-' + #playlistDTO.title + '-' + #playlistDTO.style")
    public Result<PageResult<PlaylistVO>> getAllPlaylists(PlaylistDTO playlistDTO) {
        // 分页查询
        Page<Playlist> page = new Page<>(playlistDTO.getPageNum(), playlistDTO.getPageSize());
        QueryWrapper<Playlist> queryWrapper = new QueryWrapper<>();
        // 根据 playlistDTO 的条件构建查询条件
        if (playlistDTO.getTitle() != null) {
            queryWrapper.like("title", playlistDTO.getTitle());
        }
        if (playlistDTO.getStyle() != null) {
            queryWrapper.eq("style", playlistDTO.getStyle());
        }

        IPage<Playlist> playlistPage = playlistMapper.selectPage(page, queryWrapper);
        if (playlistPage.getRecords().isEmpty()) {
            return Result.success(MessageConstant.DATA_NOT_FOUND, new PageResult<>(0L, null));
        }

        // 转换为 PlaylistVO
        List<PlaylistVO> playlistVOList = playlistPage.getRecords().stream()
                .map(playlist -> {
                    PlaylistVO playlistVO = new PlaylistVO();
                    BeanUtils.copyProperties(playlist, playlistVO);
                    return playlistVO;
                }).toList();

        return Result.success(new PageResult<>(playlistPage.getTotal(), playlistVOList));
    }

    // TODO 推荐方式需要修改，这个太低级了
    /**
     * 获取推荐歌单
     * 推荐歌单的数量为 10
     *
     * @param request HttpServletRequest，用于获取请求头中的 token
     * @return 随机歌单列表
     */
    @Override
    public Result<List<PlaylistVO>> getRecommendedPlaylists(HttpServletRequest request) {

        threadLocalUtil.setThreadLocalByToken(request);

        Long userId = ThreadLocalUtil.getUserId();

        // 用户未登录，返回随机歌单
        if (userId == null) {
            return Result.success(playlistMapper.getRandomPlaylists(10));
        }

        // 获取用户收藏的歌单 ID
        List<Long> favoritePlaylistIds = userFavoriteMapper.getFavoritePlaylistIdsByUserId(userId);
        if (favoritePlaylistIds.isEmpty()) {
            return Result.success(playlistMapper.getRandomPlaylists(10)); // 如果用户没有收藏歌单，返回随机歌单
        }

        // 查询用户收藏的歌单风格并统计频率
        List<String> favoriteStyles = playlistMapper.getFavoritePlaylistStyles(favoritePlaylistIds);
        List<Long> favoriteStyleIds = userFavoriteMapper.getFavoriteIdsByStyle(favoriteStyles);
        Map<Long, Long> styleFrequency = favoriteStyleIds.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        // 按风格出现次数降序排序
        List<Long> sortedStyleIds = styleFrequency.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 根据排序后的风格推荐歌单（排除已收藏歌单）
        List<PlaylistVO> recommendedPlaylists = playlistMapper.getRecommendedPlaylistsByStyles(sortedStyleIds, favoritePlaylistIds, 10);

        // 如果推荐的歌单不足 10 个，则用随机歌单填充
        if (recommendedPlaylists.size() < 10) {
            List<PlaylistVO> randomPlaylists = playlistMapper.getRandomPlaylists(10);
            Set<Long> addedPlaylistIds = recommendedPlaylists.stream().map(PlaylistVO::getPlaylistId).collect(Collectors.toSet());

            for (PlaylistVO playlist : randomPlaylists) {
                if (recommendedPlaylists.size() >= 10) break;
                if (!addedPlaylistIds.contains(playlist.getPlaylistId())) {
                    recommendedPlaylists.add(playlist);
                }
            }
        }

        return Result.success(recommendedPlaylists);
    }

    /**
     * 获取歌单详情
     *
     * @param playlistId 歌单id
     * @param request    HttpServletRequest，用于获取请求头中的 token
     * @return 歌单详情
     */
    @Override
    @Cacheable(key = "#playlistId")
    public Result<PlaylistDetailVO> getPlaylistDetail(Long playlistId, HttpServletRequest request) {
        PlaylistDetailVO playlistDetailVO = playlistMapper.getPlaylistDetailById(playlistId);

        // 设置默认状态
        List<SongVO> songVOList = playlistDetailVO.getSongs();
        songVOList.forEach(songVO -> songVO.setLikeStatus(LikeStatusEnum.DEFAULT.getId()));
        playlistDetailVO.setLikeStatus(LikeStatusEnum.DEFAULT.getId());

        threadLocalUtil.setThreadLocalByToken(request);

        // 如果 token 解析成功且用户为登录状态，进一步操作
        Long userId = ThreadLocalUtil.getUserId();
        if ( userId != null ) {
            // 获取用户收藏的歌单
            UserFavorite favoritePlaylist = userFavoriteMapper.selectOne(new QueryWrapper<UserFavorite>()
                    .eq("user_id", userId)
                    .eq("type", 1)
                    .eq("playlist_id", playlistId));
            if (favoritePlaylist != null) {
                playlistDetailVO.setLikeStatus(LikeStatusEnum.LIKE.getId());
            }

            // 获取用户收藏的歌曲
            List<UserFavorite> favoriteSongs = userFavoriteMapper.selectList(new QueryWrapper<UserFavorite>()
                    .eq("user_id", userId)
                    .eq("type", 0));

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


        return Result.success(playlistDetailVO);
    }

}
