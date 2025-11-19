package com.kay.music.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kay.music.pojo.dto.*;
import com.kay.music.pojo.entity.User;
import com.kay.music.pojo.vo.UserManagementVO;
import com.kay.music.pojo.vo.UserVO;
import com.kay.music.result.PageResult;
import com.kay.music.result.Result;

import java.util.List;

/**
 * @author Kay
 * @date 2025/11/17 18:05
 */
public interface IUserService extends IService<User> {
    Result<Long> getAllUsersCount();

    Result<PageResult<UserManagementVO>> getAllUsers(UserSearchDTO userSearchDTO);

    Result addUser(UserAddDTO userAddDTO);

    Result updateUser(UserDTO userDTO);

    Result updateUserStatus(Long userId, Integer userStatus);

    Result deleteUser(Long userId);

    Result deleteUsers(List<Long> userIds);

    Result sendVerificationCode(String email);

    boolean verifyVerificationCode(String email, String verificationCode);

    Result register(UserRegisterDTO userRegisterDTO);

    Result login(UserLoginDTO userLoginDTO);

    Result<UserVO> userInfo();

    Result updateUserInfo(UserDTO userDTO);

    Result updateUserAvatar(String avatarUrl);

    Result updateUserPassword(UserPasswordDTO userPasswordDTO, String token);
}
