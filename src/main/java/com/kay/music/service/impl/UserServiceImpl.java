package com.kay.music.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kay.music.mapper.UserMapper;
import com.kay.music.pojo.entity.User;
import com.kay.music.result.Result;
import com.kay.music.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.stereotype.Service;

/**
 * @author Kay
 * @date 2025/11/17 18:06
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "userCache")
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final UserMapper userMapper;


    /**
     * @Description: 获取所有用户数量
     * @return: Result<Long> 用户数量
     * @Author: Kay
     * @date:   2025/11/17 18:12
     */
    @Override
    public Result<Long> getAllUsersCount() {
        return Result.success(userMapper.selectCount(new QueryWrapper<>()));
    }
}
