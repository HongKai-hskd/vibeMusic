package com.kay.music.controller;

import com.kay.music.pojo.dto.FeedbackDTO;
import com.kay.music.pojo.entity.Feedback;
import com.kay.music.result.PageResult;
import com.kay.music.result.Result;
import com.kay.music.service.IFeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Kay
 * @date 2025/11/22 16:00
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "反馈接口")
public class FeedbackController {

    private final IFeedbackService feedbackService;

    /**
     * 获取反馈列表
     *
     * @return 反馈列表
     */
    @Operation(summary = "获取反馈列表")
    @PostMapping("/admin/getAllFeedbacks")
    public Result<PageResult<Feedback>> getAllFeedbacks(@RequestBody FeedbackDTO feedbackDTO) {
        return feedbackService.getAllFeedbacks(feedbackDTO);
    }

    /**
     * 删除反馈
     *
     * @param feedbackId 反馈id
     * @return 结果
     */
    @Operation(summary = "删除反馈")
    @DeleteMapping("/admin/deleteFeedback/{id}")
    public Result deleteFeedback(@PathVariable("id") Long feedbackId) {
        return feedbackService.deleteFeedback(feedbackId);
    }

    /**
     * 批量删除反馈
     *
     * @param feedbackIds 反馈id列表
     * @return 结果
     */
    @Operation(summary = "批量删除反馈")
    @DeleteMapping("/admin/deleteFeedbacks")
    public Result deleteFeedbacks(@RequestBody List<Long> feedbackIds) {
        return feedbackService.deleteFeedbacks(feedbackIds);
    }

    /**
     * 添加反馈
     *
     * @param content 反馈内容
     * @return 结果
     */
    @Operation(summary = "添加反馈")
    @PostMapping("/feedback/addFeedback")
    public Result addFeedback(@RequestParam(value = "content") String content) {
        return feedbackService.addFeedback(content);
    }

}
