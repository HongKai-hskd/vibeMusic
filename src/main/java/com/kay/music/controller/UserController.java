package com.kay.music.controller;

import com.kay.music.constant.MessageConstant;
import com.kay.music.pojo.dto.*;
import com.kay.music.pojo.vo.UserVO;
import com.kay.music.result.Result;
import com.kay.music.service.IUserService;
import com.kay.music.service.MinioService;
import com.kay.music.utils.ThreadLocalUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    private final MinioService minioService;


    /**
     * @Description: 测试 ThreadLocalUtil
     * @Author: Kay
     * @date: 2025/11/16 17:53
     */
    @GetMapping("/test")
    @Operation(summary = "测试 ThreadLocalUtil ")
    public Result test(){
        log.info("ThreadLocal：{}", ThreadLocalUtil.get().toString());
        return Result.success("ThreadLocal：" + ThreadLocalUtil.get().toString());
    }

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

    /**
     * @Description: 用户登录
     * @Author: Kay
     * @date:   2025/11/19 11:29
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result login(@RequestBody @Valid UserLoginDTO userLoginDTO){
        return userService.login(userLoginDTO);
    }

    /**
     * @Description: 获取用户信息
     * @Author: Kay
     * @date:   2025/11/19 11:39
     */
    @Operation(summary = "获取用户信息")
    @GetMapping("/getUserInfo")
    public Result<UserVO> getUserInfo() {
        return userService.userInfo();
    }

    /**
     * @Description: 更新用户信息
     * @Author: Kay
     * @date:   2025/11/19 20:22
     */
    @Operation(summary = "更新用户信息")
    @PutMapping("/updateUserInfo")
    public Result updateUserInfo(@RequestBody @Valid UserDTO userDTO){
        return userService.updateUserInfo(userDTO);
    }

    /**
     * @Description:
     * @Author: Kay
     * @date:   2025/11/19 21:26
     */
    @Operation(summary = "更新用户头像，并上传 minio")
    @PatchMapping("/updateUserAvatar")  // HTTP 请求方式之一：更新部分字段
    public Result updateUserAvatar(@RequestParam("avatar") MultipartFile avatar){
        String avatarUrl = minioService.uploadFile(avatar, "users");  // 上传到 users 目录
        return userService.updateUserAvatar(avatarUrl);
    }

    
    /**
     * @Description: 更新用户密码，并注销 token
     * @param: userPasswordDTO 用户密码信息
     * @param: token 认证token
     * @Author: Kay
     * @date:   2025/11/19 21:40
     */
    @Operation(summary = "更新用户密码，并注销 token")
    @PatchMapping("/updateUserPassword")
    public Result updateUserPassword(@RequestBody @Valid UserPasswordDTO userPasswordDTO,
                                     @RequestHeader("Authorization") String token){
        return userService.updateUserPassword(userPasswordDTO, token);
    }


    /**
     * @Description: 根据邮箱验证码重置密码
     * @Author: Kay
     * @date:   2025/11/19 21:52
     */
    @Operation(summary = "根据邮箱验证码重置密码")
    @PatchMapping("/resetUserPassword")
    public Result resetUserPassword(@RequestBody @Valid UserResetPasswordDTO userResetPasswordDTO){

        // 验证验证码是否正确
        boolean isCodeValid = userService.verifyVerificationCode(userResetPasswordDTO.getEmail(), userResetPasswordDTO.getVerificationCode());
        if (!isCodeValid) {
            return Result.error(MessageConstant.VERIFICATION_CODE + MessageConstant.INVALID);
        }
        return userService.resetUserPassword(userResetPasswordDTO);
    }



}
