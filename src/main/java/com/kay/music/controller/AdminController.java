package com.kay.music.controller;

import com.kay.music.pojo.dto.*;
import com.kay.music.pojo.entity.Artist;
import com.kay.music.pojo.vo.ArtistNameVO;
import com.kay.music.pojo.vo.SongAdminVO;
import com.kay.music.pojo.vo.UserManagementVO;
import com.kay.music.result.PageResult;
import com.kay.music.result.Result;
import com.kay.music.service.*;
import com.kay.music.utils.ThreadLocalUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
    private final IArtistService artistService;
    private final MinioService minioService;
    private final ISongService songService;

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

    /**
     * @Description: 更新用户状态
     * @param: userId
     * @param: userStatus 用户状态
     * @Author: Kay
     * @date:   2025/11/17 20:34
     */
    @PatchMapping("/updateUserStatus/{id}/{status}")
    @Operation(summary = "更新用户状态")
    public Result updateUserStatus(@PathVariable("id") Long userId, @PathVariable("status") Integer userStatus) {
        return userService.updateUserStatus(userId, userStatus);
    }

    /**
     * @Description: 删除用户
     * @param: userId
     * @Author: Kay
     * @date:   2025/11/17 20:42
     */
    @DeleteMapping("/deleteUser/{id}")
    @Operation(summary = "删除用户")
    public Result deleteUser(@PathVariable("id") Long userId) {
        return userService.deleteUser(userId);
    }

    /**
     * @Description: 批量删除用户
     * @Author: Kay
     * @date:   2025/11/17 20:46
     */
    @DeleteMapping("/deleteUsers")
    @Operation(summary = "批量删除用户")
    // 前端传的时候直接传 ： [2, 3] 就可以了
    public Result deleteUsers(@RequestBody List<Long> userIds) {
        log.info("批量删除用户：{}" , userIds);
        return userService.deleteUsers(userIds);
    }

    /**********************************************************************************************/

    /**
     * @Description: 获取所有歌手数量
     * @Author: Kay
     * @date:   2025/11/21 11:11
     */
    @Operation(summary = "获取所有歌手数量")
    @GetMapping("/getAllArtistsCount")
    public Result<Long> getAllArtistsCount(@RequestParam(required = false) Integer gender,
                                           @RequestParam(required = false) String area){

        return artistService.getAllArtistsCount(gender, area);
    }

    /**
     * @Description: 获取所有歌手信息
     * @Author: Kay
     * @date:   2025/11/21 11:24
     */
    @Operation(summary = "获取所有歌手信息")
    @PostMapping("/getAllArtists")
    public Result<PageResult<Artist>> getAllArtists(@RequestBody ArtistDTO artistDTO) {
        return artistService.getAllArtistsAndDetail(artistDTO);
    }

    /**
     * @Description: 添加歌手
     * @Author: Kay
     * @date:   2025/11/21 20:46
     */
    @Operation(summary = "添加歌手")
    @PostMapping("/addArtist")
    public Result addArtist(@RequestBody ArtistAddDTO artistAddDTO){
        return artistService.addArtist(artistAddDTO);
    }

    /**
     * @Description: 更新歌手信息
     * @Author: Kay
     * @date:   2025/11/21 20:49
     */
    @Operation(summary = "添加歌手")
    @PutMapping("/updateArtist")
    public Result updateArtist(@RequestBody ArtistUpdateDTO artistUpdateDTO) {
        return artistService.updateArtist(artistUpdateDTO);
    }

    /**
     * @Description: 更新歌手头像
     * @Author: Kay
     * @date:   2025/11/21 20:54
     */
    @Operation(summary = "更新歌手头像")
    @PatchMapping("/updateArtistAvatar/{id}")
    public Result updateArtistAvatar(@PathVariable("id") Long artistId, @RequestParam("avatar") MultipartFile avatar) {
        String avatarUrl = minioService.uploadFile(avatar, "artists");  // 上传到 artists 目录
        return artistService.updateArtistAvatar(artistId, avatarUrl);
    }

    /**
     * @Description: 删除歌手
     * @Author: Kay
     * @date:   2025/11/21 20:57
     */
    @Operation(summary = "删除歌手")
    @DeleteMapping("/deleteArtist/{id}")
    public Result deleteArtist(@PathVariable("id") Long artistId) {
        return artistService.deleteArtist(artistId);
    }

    /**
     * @Description: 批量删除歌手
     * @Author: Kay
     * @date:   2025/11/21 20:59
     */
    @Operation(summary = "批量删除歌手")
    @DeleteMapping("/deleteArtists")
    public Result deleteArtists(@RequestBody List<Long> artistIds) {
        return artistService.deleteArtists(artistIds);
    }

    /**********************************************************************************************/

    /**
     * @Description: 获取所有歌曲的数量
     * @Author: Kay
     * @date:   2025/11/21 21:30
     */
    @Operation(summary = "获取所有歌曲的数量")
    @GetMapping("/getAllSongsCount")
    public Result<Long> getAllSongsCount(@RequestParam(required = false) String style) {
        return songService.getAllSongsCount(style);
    }

    /**
     * @Description: 获取所有歌手id和名称
     * @Author: Kay
     * @date:   2025/11/21 21:32
     */
    @GetMapping("/getAllArtistNames")
    @Operation(summary = "获取所有歌手id和名称")
    public Result<List<ArtistNameVO>> getAllArtistNames() {
        return artistService.getAllArtistNames();
    }

    /**
     * @Description: 根据歌手id获取其歌曲信息
     * @Author: Kay
     * @date:   2025/11/21 21:34
     */
    @Operation(summary = "根据歌手id获取其歌曲信息")
    @PostMapping("/getAllSongsByArtist")
    public Result<PageResult<SongAdminVO>> getAllSongsByArtist(@RequestBody SongAndArtistDTO songDTO) {
        return songService.getAllSongsByArtist(songDTO);
    }

    /**
     * @Description: 添加歌曲信息
     * @Author: Kay
     * @date:   2025/11/21 21:40
     */
    @Operation(summary = "添加歌曲信息")
    @PostMapping("/addSong")
    public Result addSong(@RequestBody SongAddDTO songAddDTO) {
        return songService.addSong(songAddDTO);
    }

    /**
     * @Description: 修改歌曲信息
     * @Author: Kay
     * @date:   2025/11/21 21:47
     */
    @Operation(summary = "修改歌曲信息")
    @PutMapping("/updateSong")
    public Result UpdateSong(@RequestBody SongUpdateDTO songUpdateDTO) {
        return songService.updateSong(songUpdateDTO);
    }



}
