package com.kay.music.controller;

import com.kay.music.constant.MessageConstant;
import com.kay.music.pojo.dto.UserRegisterDTO;
import com.kay.music.result.Result;
import com.kay.music.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @author Kay
 * @date 2025/11/17 21:00
 */
@RestController
@RequestMapping("/user")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "用户管理接口")
public class UserController {

    private final IUserService userService;


    /**
     * @Description: 发送验证码
     * @param: email
     * @Author: Kay
     * @date:   2025/11/17 21:02
     */
    @Operation(summary = "发送邮件验证码")
    @GetMapping("/sendVerificationCode")
    // @RequestParam 即 URL 中 ? 后面的键值对
    public Result sendVerificationCode(@RequestParam @Email String email) {
        return userService.sendVerificationCode(email);
    }

    /**
     * @Description: 用户注册
     * @Author: Kay
     * @date:   2025/11/18 23:44
     */
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result register(@RequestBody @Valid UserRegisterDTO userRegisterDTO){
        // 验证验证码是否正确
        boolean isCodeValid = userService.verifyVerificationCode(userRegisterDTO.getEmail(), userRegisterDTO.getVerificationCode());
        if (!isCodeValid) {
            return Result.error(MessageConstant.VERIFICATION_CODE + MessageConstant.INVALID);
        }
        return userService.register(userRegisterDTO);
    }




}
