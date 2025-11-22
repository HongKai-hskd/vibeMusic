package com.kay.music.controller;

import com.kay.music.pojo.dto.CommentPlaylistDTO;
import com.kay.music.pojo.dto.CommentSongDTO;
import com.kay.music.result.Result;
import com.kay.music.service.ICommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author Kay
 * @date 2025/11/22 15:42
 */
@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {

    private final ICommentService commentService;

    /**
     * 新增歌曲评论
     *
     * @param commentSongDTO 评论信息
     * @return 结果
     */
    @PostMapping("/addSongComment")
    public Result addSongComment(@RequestBody CommentSongDTO commentSongDTO) {
        return commentService.addSongComment(commentSongDTO);
    }

    /**
     * 新增歌单评论
     *
     * @param commentPlaylistDTO 评论信息
     * @return 结果
     */
    @PostMapping("/addPlaylistComment")
    public Result addPlaylistComment(@RequestBody CommentPlaylistDTO commentPlaylistDTO) {
        return commentService.addPlaylistComment(commentPlaylistDTO);
    }

    /**
     * 点赞评论
     *
     * @param commentId 评论id
     * @return 结果
     */
    @PatchMapping("/likeComment/{id}")
    public Result likeComment(@PathVariable("id") Long commentId) {
        return commentService.likeComment(commentId);
    }

    /**
     * 取消点赞评论
     *
     * @param commentId 评论id
     * @return 结果
     */
    @PatchMapping("/cancelLikeComment/{id}")
    public Result cancelLikeComment(@PathVariable("id") Long commentId) {
        return commentService.cancelLikeComment(commentId);
    }

    /**
     * 删除评论
     *
     * @param commentId 评论id
     * @return 结果
     */
    @DeleteMapping("/deleteComment/{id}")
    public Result deleteComment(@PathVariable("id") Long commentId) {
        return commentService.deleteComment(commentId);
    }

}