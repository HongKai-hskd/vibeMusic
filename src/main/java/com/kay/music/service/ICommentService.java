package com.kay.music.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kay.music.pojo.dto.CommentPlaylistDTO;
import com.kay.music.pojo.dto.CommentSongDTO;
import com.kay.music.pojo.entity.Comment;
import com.kay.music.result.Result;

/**
 * @author Kay
 * @date 2025/11/22 15:43
 */
public interface ICommentService extends IService<Comment> {
    Result addSongComment(CommentSongDTO commentSongDTO);

    Result addPlaylistComment(CommentPlaylistDTO commentPlaylistDTO);

    Result likeComment(Long commentId);

    Result cancelLikeComment(Long commentId);

    Result deleteComment(Long commentId);
}
