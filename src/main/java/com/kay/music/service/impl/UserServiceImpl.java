package com.kay.music.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kay.music.constant.MessageConstant;
import com.kay.music.mapper.UserMapper;
import com.kay.music.pojo.dto.UserSearchDTO;
import com.kay.music.pojo.entity.User;
import com.kay.music.pojo.vo.UserManagementVO;
import com.kay.music.result.PageResult;
import com.kay.music.result.Result;
import com.kay.music.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Kay
 * @date 2025/11/17 18:06
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "userCache")
// 定义当前类所有缓存的「缓存名称」（相当于缓存的「组名 / 命名空间」），后续缓存操作都会绑定到这个名称下。
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

    /**
     * @Description: 分页查询所有用户
     * @param:  userSearchDTO 用户查询条件
     * @return: Result<PageResult<UserManagementVO>> 用户分页信息
     * @Author: Kay
     * @date:   2025/11/17 19:11
     */
    @Override
    // @Cacheable 是方法级缓存触发，key 的核心是「唯一标识查询条件」
    @Cacheable(key = "#userSearchDTO.pageNum + '-' + #userSearchDTO.pageSize + '-' + #userSearchDTO.username + '-' + #userSearchDTO.phone + '-' + #userSearchDTO.userStatus")
    public Result<PageResult<UserManagementVO>> getAllUsers(UserSearchDTO userSearchDTO) {
        // 1. 分页查询
        Page<User> page = new Page<>(userSearchDTO.getPageNum(), userSearchDTO.getPageSize());
        // 2. 根据 userSearchDTO 的条件构建查询条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        if ( userSearchDTO.getUsername() != null ) {
            queryWrapper.like(User::getUsername , userSearchDTO.getUsername());
        }
        if ( userSearchDTO.getPhone() != null ) {
            queryWrapper.like(User::getPhone , userSearchDTO.getPhone());
        }
        if ( userSearchDTO.getUserStatus() != null ) {
            queryWrapper.eq(User::getUserStatus , userSearchDTO.getUserStatus().getId());
        }
        // 3. 倒序排序
        queryWrapper.orderByDesc(User::getCreateTime);

        // 4. 查询结果
        Page<User> userPage = userMapper.selectPage(page, queryWrapper);

        // 5.1 返回结果 - 未查到
        if ( userPage.getRecords().size() == 0 ) {
            return Result.success(MessageConstant.DATA_NOT_FOUND , new PageResult<>(0L,null));
        }
        // 5.2 返回结果 - 封装结果
        List<UserManagementVO> userVOList = userPage.getRecords().stream().map(
                user -> {
                    UserManagementVO userVO = new UserManagementVO();
                    BeanUtils.copyProperties(user, userVO);
                    return userVO;
                }
        ).toList();

        return Result.success(new PageResult<>(userPage.getTotal(), userVOList));
    }
}
