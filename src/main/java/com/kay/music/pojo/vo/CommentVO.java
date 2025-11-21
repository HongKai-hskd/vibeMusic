package com.kay.music.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * @author Kay
 * @date 2025/11/21 10:30
 */

@Data
@Schema(name = "CommentVO", description = "评论VO类")
public class CommentVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "评论 id")
    private Long commentId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "用户头像")
    private String userAvatar;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "评论时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createTime;

    @Schema(description = "点赞数量")
    private Long likeCount;

}

