package com.kay.music.controller;

import com.kay.music.constant.MessageConstant;
import com.kay.music.pojo.dto.UserLoginDTO;
import com.kay.music.pojo.dto.UserRegisterDTO;
import com.kay.music.pojo.vo.UserVO;
import com.kay.music.result.Result;
import com.kay.music.service.IUserService;
import com.kay.music.utils.ThreadLocalUtil;
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


}
