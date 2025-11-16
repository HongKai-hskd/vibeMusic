package com.kay.music.controller;

import com.kay.music.pojo.dto.AdminDTO;
import com.kay.music.result.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.BindingResultUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Kay
 * @date 2025/11/15 21:44
 */
@RestController
@RequestMapping("/admin")
@Slf4j
@Tag(name = "管理员接口")
public class AdminController {


    @PostMapping("/register")
    public Result register(@RequestBody @Valid AdminDTO adminDTO , BindingResult bindingResult) {
        log.info("管理员注册：{}",adminDTO);
        // 校验失败时，返回错误信息

        return null;
    }
}
