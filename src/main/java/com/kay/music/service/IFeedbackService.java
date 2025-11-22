package com.kay.music.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kay.music.pojo.dto.FeedbackDTO;
import com.kay.music.pojo.entity.Feedback;
import com.kay.music.result.PageResult;
import com.kay.music.result.Result;

import java.util.List;

/**
 * @author Kay
 * @date 2025/11/22 16:00
 */
public interface IFeedbackService extends IService<Feedback> {
    Result<PageResult<Feedback>> getAllFeedbacks(FeedbackDTO feedbackDTO);

    Result deleteFeedback(Long feedbackId);

    Result deleteFeedbacks(List<Long> feedbackIds);

    Result addFeedback(String content);
}
