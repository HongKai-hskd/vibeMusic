package com.kay.music.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kay.music.constant.MessageConstant;
import com.kay.music.enumeration.LikeStatusEnum;
import com.kay.music.enumeration.RoleEnum;
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
import java.util.Set;
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
    public Result<Long> getAllPlaylistsCount() {
        return Result.success(playlistMapper.selectCount(null));
    }
    
    /**
     * @Description: 获取所有歌单信息
     * @Author: Kay
     * @date:   2025/11/22 15:15
     */
    @Override
    @Cacheable(key = "'admin:playlist:' + #playlistDTO.pageNum + '-' + #playlistDTO.pageSize + '-' + #playlistDTO.title", unless = "#result == null")
    public Result<PageResult<Playlist>> getAllPlaylistsInfo(PlaylistDTO playlistDTO) {
        // 分页查询
        Page<Playlist> page = new Page<>(playlistDTO.getPageNum(), playlistDTO.getPageSize());
        LambdaQueryWrapper<Playlist> queryWrapper = new LambdaQueryWrapper<>();
        // 根据 playlistDTO 的条件构建查询条件
        if (playlistDTO.getTitle() != null) {
            queryWrapper.like(Playlist::getTitle, playlistDTO.getTitle());
        }
        // 倒序排序
        queryWrapper.orderByDesc(Playlist::getPlaylistId);

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
        LambdaQueryWrapper<Playlist> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Playlist::getTitle, playlistAddDTOO.getTitle());
        if (playlistMapper.selectCount(queryWrapper) > 0) {
            return Result.error(MessageConstant.PLAYLIST + MessageConstant.ALREADY_EXISTS);
        }

        // 获取当前登录用户 ID
        Long currentUserId = ThreadLocalUtil.getUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }

        Playlist playlist = new Playlist();
        BeanUtils.copyProperties(playlistAddDTOO, playlist);
        playlist.setUserId(currentUserId); // 设置创建者 ID
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

        // 获取当前登录用户 ID
        Long currentUserId = ThreadLocalUtil.getUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }

        // 验证是否为创建者或管理员
        Playlist existingPlaylist = playlistMapper.selectById(playlistId);
        if (existingPlaylist == null) {
            return Result.error(MessageConstant.PLAYLIST + MessageConstant.NOT_FOUND);
        }
        
        String role = ThreadLocalUtil.getRole();
        boolean isAdmin = RoleEnum.ADMIN.getRole().equals(role);
        boolean isCreator = currentUserId.equals(existingPlaylist.getUserId());
        
        if (!isAdmin && !isCreator) {
            return Result.error("只有歌单创建者或管理员才能修改歌单");
        }

        Playlist playlistByTitle = playlistMapper.selectOne(new LambdaQueryWrapper<Playlist>().eq(Playlist::getTitle, playlistUpdateDTO.getTitle()));
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
        // 获取当前登录用户 ID
        Long currentUserId = ThreadLocalUtil.getUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }

        Playlist playlist = playlistMapper.selectById(playlistId);
        if (playlist == null) {
            return Result.error(MessageConstant.PLAYLIST + MessageConstant.NOT_FOUND);
        }

        // 验证是否为创建者或管理员
        String role = ThreadLocalUtil.getRole();
        boolean isAdmin = RoleEnum.ADMIN.getRole().equals(role);
        boolean isCreator = currentUserId.equals(playlist.getUserId());
        
        if (!isAdmin && !isCreator) {
            return Result.error("只有歌单创建者或管理员才能修改歌单封面");
        }

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
        // 获取当前登录用户 ID
        Long currentUserId = ThreadLocalUtil.getUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }

        // 1. 查询歌单信息，获取封面 URL
        Playlist playlist = playlistMapper.selectById(playlistId);
        if (playlist == null) {
            return Result.error(MessageConstant.PLAYLIST + MessageConstant.NOT_FOUND);
        }

        // 验证是否为创建者或管理员
        String role = ThreadLocalUtil.getRole();
        boolean isAdmin = RoleEnum.ADMIN.getRole().equals(role);
        boolean isCreator = currentUserId.equals(playlist.getUserId());
        
        if (!isAdmin && !isCreator) {
            return Result.error("只有歌单创建者或管理员才能删除歌单");
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
        // 获取当前登录用户 ID
        Long currentUserId = ThreadLocalUtil.getUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }

        List<Playlist> playlists = playlistMapper.selectBatchIds(playlistIds);
        
        // 验证所有歌单是否都是当前用户创建的或用户是管理员
        String role = ThreadLocalUtil.getRole();
        boolean isAdmin = RoleEnum.ADMIN.getRole().equals(role);
        
        if (!isAdmin) {
            for (Playlist playlist : playlists) {
                if (!currentUserId.equals(playlist.getUserId())) {
                    return Result.error("只有歌单创建者或管理员才能删除歌单，歌单 [" + playlist.getTitle() + "] 不属于您");
                }
            }
        }
        
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
    @Cacheable(key = "'playlist:list:' + #playlistDTO.pageNum + '-' + #playlistDTO.pageSize + '-' + #playlistDTO.title", unless = "#result == null")
    public Result<PageResult<PlaylistVO>> getAllPlaylists(PlaylistDTO playlistDTO) {
        // 分页查询
        Page<Playlist> page = new Page<>(playlistDTO.getPageNum(), playlistDTO.getPageSize());
        LambdaQueryWrapper<Playlist> queryWrapper = new LambdaQueryWrapper<>();
        // 根据 playlistDTO 的条件构建查询条件
        if (playlistDTO.getTitle() != null) {
            queryWrapper.like(Playlist::getTitle, playlistDTO.getTitle());
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

    // TODO: 推荐方式需要修改，当前为随机推荐，后续可以基于用户收藏、播放历史等方式实现个性化推荐
    /**
     * 获取推荐歌单
     * 推荐歌单的数量为 10
     *
     * @param request HttpServletRequest，用于获取请求头中的 token
     * @return 随机歌单列表
     */
    @Override
    @Cacheable(key = "'playlist:recommended'", unless = "#result == null")
    public Result<List<PlaylistVO>> getRecommendedPlaylists(HttpServletRequest request) {
        // 目前简化为返回随机歌单
        List<PlaylistVO> playlists = playlistMapper.getRandomPlaylists(10);
        if (playlists == null || playlists.isEmpty()) {
            // 返回空结果（也会被缓存，防止缓存穿透）
            return Result.success(MessageConstant.DATA_NOT_FOUND, null);
        }
        return Result.success(playlists);
    }

    /**
     * 获取歌单详情
     *
     * @param playlistId 歌单id
     * @param request    HttpServletRequest，用于获取请求头中的 token
     * @return 歌单详情
     */
    @Override
    @Cacheable(key = "'playlist:detail:' + #playlistId", unless = "#result == null")
    public Result<PlaylistDetailVO> getPlaylistDetail(Long playlistId, HttpServletRequest request) {
        PlaylistDetailVO playlistDetailVO = playlistMapper.getPlaylistDetailById(playlistId);
        
        // 如果歌单不存在，返回空结果（也会被缓存，防止缓存穿透）
        if (playlistDetailVO == null) {
            return Result.success(MessageConstant.PLAYLIST + MessageConstant.NOT_FOUND, null);
        }
        
        // 设置默认状态
        List<SongVO> songVOList = playlistDetailVO.getSongs();
        songVOList.forEach(songVO -> songVO.setLikeStatus(LikeStatusEnum.DEFAULT.getId()));
        playlistDetailVO.setLikeStatus(LikeStatusEnum.DEFAULT.getId());

        threadLocalUtil.setThreadLocalByToken(request);

        // 如果 token 解析成功且用户为登录状态，进一步操作
        Long userId = ThreadLocalUtil.getUserId();
        if ( userId != null ) {
            // 获取用户收藏的歌单
            UserFavorite favoritePlaylist = userFavoriteMapper.selectOne(new LambdaQueryWrapper<UserFavorite>()
                    .eq(UserFavorite::getUserId, userId)
                    .eq(UserFavorite::getType, 1)
                    .eq(UserFavorite::getPlaylistId, playlistId));
            if (favoritePlaylist != null) {
                playlistDetailVO.setLikeStatus(LikeStatusEnum.LIKE.getId());
            }

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


        return Result.success(playlistDetailVO);
    }

}
