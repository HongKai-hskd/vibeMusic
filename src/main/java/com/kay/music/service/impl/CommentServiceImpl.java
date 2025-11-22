package com.kay.music.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kay.music.constant.MessageConstant;
import com.kay.music.mapper.CommentMapper;
import com.kay.music.pojo.dto.CommentPlaylistDTO;
import com.kay.music.pojo.dto.CommentSongDTO;
import com.kay.music.pojo.entity.Comment;
import com.kay.music.result.Result;
import com.kay.music.service.ICommentService;
import com.kay.music.utils.ThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * @author Kay
 * @date 2025/11/22 15:49
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

    private final CommentMapper commentMapper;

    /**
     * 添加歌曲评论
     *
     * @param commentSongDTO 歌曲评论DTO
     * @return Result
     */
    @Override
    @CacheEvict(cacheNames = "songCache", allEntries = true)
    public Result addSongComment(CommentSongDTO commentSongDTO) {

        Long userId = ThreadLocalUtil.getUserId();

        Comment comment = new Comment();
        comment.setUserId(userId).setSongId(commentSongDTO.getSongId())
                .setContent(commentSongDTO.getContent()).setType(0)
                .setCreateTime(LocalDateTime.now()).setLikeCount(0L);

        if (commentMapper.insert(comment) == 0) {
            return Result.error(MessageConstant.ADD + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.ADD + MessageConstant.SUCCESS);
    }

    /**
     * 添加歌单评论
     *
     * @param commentPlaylistDTO 歌单评论DTO
     * @return Result
     */
    @Override
    @CacheEvict(cacheNames = "playlistCache", allEntries = true)
    public Result addPlaylistComment(CommentPlaylistDTO commentPlaylistDTO) {
        Long userId = ThreadLocalUtil.getUserId();

        Comment comment = new Comment();
        comment.setUserId(userId).setPlaylistId(commentPlaylistDTO.getPlaylistId())
                .setContent(commentPlaylistDTO.getContent()).setType(1)
                .setCreateTime(LocalDateTime.now()).setLikeCount(0L);

        if (commentMapper.insert(comment) == 0) {
            return Result.error(MessageConstant.ADD + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.ADD + MessageConstant.SUCCESS);
    }


    // TODO 点赞/取消点赞逻辑存在以下问题：
    // 1. 无用户维度：当前只根据 commentId 操作 likeCount，同一用户可以无限次点赞或取消点赞，缺少幂等校验。
    // 2. 取消点赞不校验是否点过赞：任何人都可以直接减少 likeCount，可能导致未点赞用户“取消点赞”以及计数为负数。
    // 3. 并发不安全：先查询再 +1/-1 再更新，在高并发下容易出现计数不准确（丢赞或重复计数）。
    //
    // TODO 修改方向：
    // 1. 引入用户维度：接收当前登录用户信息，增加 comment_like 关系表（comment_id, user_id），限制一人只能点赞一次。
    // 2. 点赞前先检查是否已点赞：已点赞则不重复增加 likeCount；取消点赞前校验是否存在点赞记录，不存在则不扣减并保证 likeCount 不为负数。
    // 3. 优化并发更新方式：使用乐观锁（版本号）或数据库层自增/自减
    //    示例：UPDATE comment SET like_count = like_count + 1 WHERE id = ?;
    //    避免“查一遍再改”的读写竞争问题。


    /**
     * 点赞评论
     *
     * @param commentId 评论ID
     * @return Result
     */
    @Override
    @CacheEvict(cacheNames = {"songCache", "playlistCache"}, allEntries = true)
    public Result likeComment(Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            return Result.error(MessageConstant.NOT_FOUND);
        }
        if (comment.getLikeCount() == null) {
            comment.setLikeCount(0L);
        }
        comment.setLikeCount(comment.getLikeCount() + 1);

        if (commentMapper.updateById(comment) == 0) {
            return Result.error(MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.SUCCESS);
    }

    /**
     * 取消点赞评论
     *
     * @param commentId 评论ID
     * @return Result
     */
    @Override
    @CacheEvict(cacheNames = {"songCache", "playlistCache"}, allEntries = true)
    public Result cancelLikeComment(Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            return Result.error(MessageConstant.NOT_FOUND);
        }
        if (comment.getLikeCount() == null) {
            comment.setLikeCount(0L);
        }
        comment.setLikeCount(comment.getLikeCount() - 1);

        if (commentMapper.updateById(comment) == 0) {
            return Result.error(MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.SUCCESS);
    }

    /**
     * 删除评论
     *
     * @param commentId 评论ID
     * @return Result
     */
    @Override
    @CacheEvict(cacheNames = {"songCache", "playlistCache"}, allEntries = true)
    public Result deleteComment(Long commentId) {
        Long userId = ThreadLocalUtil.getUserId();

        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            return Result.error(MessageConstant.NOT_FOUND);
        }
        if (!Objects.equals(comment.getUserId(), userId)) {
            return Result.error(MessageConstant.NO_PERMISSION);
        }

        if (commentMapper.deleteById(commentId) == 0) {
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }
}
