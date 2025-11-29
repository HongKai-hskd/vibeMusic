package com.kay.music.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kay.music.mapper.PlaylistBindingMapper;
import com.kay.music.mapper.PlaylistMapper;
import com.kay.music.pojo.entity.Playlist;
import com.kay.music.pojo.entity.PlaylistBinding;
import com.kay.music.enumeration.RoleEnum;
import com.kay.music.result.Result;
import com.kay.music.service.IPlaylistBindingService;
import com.kay.music.utils.ThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Kay
 * @date 2025/11/29
 */
@Service
@RequiredArgsConstructor
public class PlaylistBindingServiceImpl extends ServiceImpl<PlaylistBindingMapper, PlaylistBinding> implements IPlaylistBindingService {

    private final PlaylistMapper playlistMapper;

    @Override
    @Transactional
    public Result addSongToPlaylist(Long playlistId, Long songId) {
        // 获取当前登录用户 ID
        Long currentUserId = ThreadLocalUtil.getUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }

        // 查询歌单信息
        Playlist playlist = playlistMapper.selectById(playlistId);
        if (playlist == null) {
            return Result.error("歌单不存在");
        }

        // 验证是否为创建者或管理员
        String role = ThreadLocalUtil.getRole();
        boolean isAdmin = RoleEnum.ADMIN.getRole().equals(role);
        boolean isCreator = currentUserId.equals(playlist.getUserId());
        
        if (!isAdmin && !isCreator) {
            return Result.error("只有歌单创建者或管理员才能添加歌曲");
        }

        // 检查歌曲是否已在歌单中
        LambdaQueryWrapper<PlaylistBinding> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlaylistBinding::getPlaylistId, playlistId)
               .eq(PlaylistBinding::getSongId, songId);
        
        if (this.count(wrapper) > 0) {
            return Result.error("歌曲已在歌单中");
        }

        // 添加歌曲到歌单
        PlaylistBinding binding = new PlaylistBinding();
        binding.setPlaylistId(playlistId);
        binding.setSongId(songId);
        
        boolean saved = this.save(binding);
        if (saved) {
            return Result.success("添加成功");
        } else {
            return Result.error("添加失败");
        }
    }

    @Override
    @Transactional
    public Result removeSongFromPlaylist(Long playlistId, Long songId) {
        // 获取当前登录用户 ID
        Long currentUserId = ThreadLocalUtil.getUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }

        // 查询歌单信息
        Playlist playlist = playlistMapper.selectById(playlistId);
        if (playlist == null) {
            return Result.error("歌单不存在");
        }

        // 验证是否为创建者或管理员
        String role = ThreadLocalUtil.getRole();
        boolean isAdmin = RoleEnum.ADMIN.getRole().equals(role);
        boolean isCreator = currentUserId.equals(playlist.getUserId());
        
        if (!isAdmin && !isCreator) {
            return Result.error("只有歌单创建者或管理员才能移除歌曲");
        }

        // 删除绑定关系
        LambdaQueryWrapper<PlaylistBinding> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlaylistBinding::getPlaylistId, playlistId)
               .eq(PlaylistBinding::getSongId, songId);
        
        boolean removed = this.remove(wrapper);
        if (removed) {
            return Result.success("移除成功");
        } else {
            return Result.error("移除失败，歌曲可能不在歌单中");
        }
    }
}
