package com.kay.music.controller;

import com.kay.music.result.Result;
import com.kay.music.service.IUserService;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Kay
 * @date 2025/11/17 21:00
 */
@RestController
@RequestMapping("/user")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private IUserService userService;


    /**
     * @Description: 发送验证码
     * @param: email
     * @Author: Kay
     * @date:   2025/11/17 21:02
     */
    @GetMapping("/sendVerificationCode")
    // @RequestParam 即 URL 中 ? 后面的键值对
    public Result sendVerificationCode(@RequestParam @Email String email) {
        return userService.sendVerificationCode(email);
    }
}
