package com.kay.music.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kay.music.pojo.dto.UserAddDTO;
import com.kay.music.pojo.dto.UserSearchDTO;
import com.kay.music.pojo.entity.User;
import com.kay.music.pojo.vo.UserManagementVO;
import com.kay.music.result.PageResult;
import com.kay.music.result.Result;

/**
 * @author Kay
 * @date 2025/11/17 18:05
 */
public interface IUserService extends IService<User> {
    Result<Long> getAllUsersCount();

    Result<PageResult<UserManagementVO>> getAllUsers(UserSearchDTO userSearchDTO);

    Result addUser(UserAddDTO userAddDTO);
}
