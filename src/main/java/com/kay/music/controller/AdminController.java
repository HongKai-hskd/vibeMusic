package com.kay.music.controller;

import com.kay.music.pojo.dto.AdminDTO;
import com.kay.music.pojo.dto.UserAddDTO;
import com.kay.music.pojo.dto.UserDTO;
import com.kay.music.pojo.dto.UserSearchDTO;
import com.kay.music.pojo.vo.UserManagementVO;
import com.kay.music.result.PageResult;
import com.kay.music.result.Result;
import com.kay.music.service.IAdminService;
import com.kay.music.service.IUserService;
import com.kay.music.utils.ThreadLocalUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @author Kay
 * @date 2025/11/15 21:44
 */
@RestController
@RequestMapping("/admin")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "管理员接口")
public class AdminController {

    private final IAdminService adminService;
    private final IUserService userService;

    /**
     * @Author: Kay
     * @date:   2025/11/16 16:20
     */
    @PostMapping("/register")
    @Operation(summary = "管理员注册")
    public Result register(@RequestBody @Valid AdminDTO adminDTO) {
        log.info("管理员注册：{}",adminDTO);
        return adminService.register(adminDTO);
    }

    /**
     * @Author: Kay
     * @date:   2025/11/16 16:22
     */
    @PostMapping("/login")
    @Operation(summary = "管理员登录")
    public Result login(@RequestBody @Valid AdminDTO adminDTO) {
        log.info("管理员登录：{}",adminDTO);
        return adminService.login(adminDTO);
    }

    /**
     * @Author: Kay
     * @date:   2025/11/16 16:55
     */
    @PostMapping("/logout")
    @Operation(summary = "管理员退出登录")
    // 从 HTTP 请求的「请求头（Request Header）」中，提取名为 Authorization 的字段值
    public Result logout(@RequestHeader("Authorization") String token ) {
        log.info("管理员退出登录：{}",token);
        return adminService.logout(token);
    }

    /**
     * @Description: 测试 ThreadLocalUtil
     * @Author: Kay
     * @date: 2025/11/16 17:53
     */
    @GetMapping("/test")
    @Operation(summary = "测试 ThreadLocalUtil ")
    public Result test(){
        log.info("ThreadLocal：{}",ThreadLocalUtil.get().toString());
        return Result.success("ThreadLocal：" + ThreadLocalUtil.get().toString());
    }

    /**********************************************************************************************/

    /**
     * @Description: 获取所有用户数量
     * @return: Result<Long>  用户数量
     * @Author: Kay
     * @date:   2025/11/17 18:09
     */
    @Operation(summary = "获取所有用户数量")
    @GetMapping("/getAllUsersCount")
    public Result<Long> getAllUsersCount() {
        return userService.getAllUsersCount();
    }

    /**
     * @Param:  UserSearchDTO 用户搜索条件
     * @Author: Kay
     * @date:   2025/11/17 19:09
     */
    @Operation(summary = "获取所有用户信息")
    @PostMapping("/getAllUsers")
    public Result<PageResult<UserManagementVO>> getAllUsers(@RequestBody UserSearchDTO userSearchDTO){
        return userService.getAllUsers(userSearchDTO);
    }

    /**
     * @Description: 新增用户
     * @param: userAddDTO 用户注册信息
     * @Author: Kay
     * @date:   2025/11/17 19:31
     */
    @PostMapping("/addUser")
    @Operation(summary = "新增用户")
    public Result addUser(@RequestBody @Valid UserAddDTO userAddDTO) {
        log.info("新增用户:{}",userAddDTO);
        return userService.addUser(userAddDTO);
    }

    /**
     * @Description: 更新用户信息
     * @Author: Kay
     * @date:   2025/11/17 20:00
     */
    @PutMapping("/updateUser")
    @Operation(summary = "修改用户信息")
    public Result updateUser(@RequestBody @Valid UserDTO userDTO) {
        log.info("更新用户：{}",userDTO);
        return userService.updateUser(userDTO);
    }



}
