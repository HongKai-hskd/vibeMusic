package com.kay.music.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kay.music.constant.JwtClaimsConstant;
import com.kay.music.constant.MessageConstant;
import com.kay.music.enumeration.RoleEnum;
import com.kay.music.enumeration.UserStatusEnum;
import com.kay.music.mapper.UserMapper;
import com.kay.music.pojo.dto.*;
import com.kay.music.pojo.entity.User;
import com.kay.music.pojo.vo.UserManagementVO;
import com.kay.music.pojo.vo.UserVO;
import com.kay.music.result.PageResult;
import com.kay.music.result.Result;
import com.kay.music.service.EmailService;
import com.kay.music.service.IUserService;
import com.kay.music.utils.JwtUtil;
import com.kay.music.utils.ThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
    private final EmailService emailService;
    private final StringRedisTemplate stringRedisTemplate;
    private final JwtUtil jwtUtil;

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

    /**
     * @Description: 添加用户
     * @param: userAddDTO
     * @return: Result message
     * @Author: Kay
     * @date:   2025/11/17 19:36
     */
    @Override
    @CacheEvict(cacheNames = "userCache", allEntries = true)
    // 清除名为 userCache 的缓存组下所有缓存数据，避免新增用户后，之前查询用户的缓存（如分页 / 多条件查询缓存）返回旧数据，保证缓存与数据库数据一致性。
    public Result addUser(UserAddDTO userAddDTO) {
        // 1.0 判断能否新增
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername , userAddDTO.getUsername())
                .or()
                .eq(User::getPhone , userAddDTO.getPhone())
                .or()
                .eq(User::getEmail , userAddDTO.getEmail());
        // 1.1 用户名、电话、邮箱 不能重复
        List<User> existingUsers = userMapper.selectList(queryWrapper);
        if ( existingUsers != null && !existingUsers.isEmpty()) {
            for (User user : existingUsers) {
                if (user.getUsername().equals(userAddDTO.getUsername())) {
                    return Result.error(MessageConstant.USERNAME + MessageConstant.ALREADY_EXISTS);
                }
                if (user.getPhone().equals(userAddDTO.getPhone())) {
                    return Result.error(MessageConstant.PHONE + MessageConstant.ALREADY_EXISTS);
                }
                if (user.getEmail().equals(userAddDTO.getEmail())) {
                    return Result.error(MessageConstant.EMAIL + MessageConstant.ALREADY_EXISTS);
                }
            }
        }
        // 2.1 密码加密 , 属性拷贝
        String passwordMD5 = DigestUtils.md5DigestAsHex(userAddDTO.getPassword().getBytes());
        User user = new User();
        BeanUtils.copyProperties(userAddDTO , user);
        user.setPassword(passwordMD5)
                .setCreateTime(LocalDateTime.now())
                .setUpdateTime(LocalDateTime.now());
        // 2.2 前端传递的用户状态（1：启用，0：禁用）需反转  （前端的屎山）
        if (userAddDTO.getUserStatus().getId() == 1) {
            user.setUserStatus(UserStatusEnum.ENABLE);  // 数据库（0：启用）
        } else if (userAddDTO.getUserStatus().getId() == 0) {
            user.setUserStatus(UserStatusEnum.DISABLE);    // 数据库（1：禁用）
        }
        // 3. 返回结果
        if (userMapper.insert(user) == 0) {
            return Result.error(MessageConstant.ADD + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.ADD + MessageConstant.SUCCESS);
    }

    /**
     * @Description: 更新用户
     * @return: Result message
     * @Author: Kay
     * @date:   2025/11/17 20:01
     */
    @Override
    @CacheEvict(cacheNames = "userCache", allEntries = true)
    public Result updateUser(UserDTO userDTO) {
        Long userId = userDTO.getUserId();

        // 0. 校验是否有权限进行修改 （ Admin 还有 自己 有权限修改自己的信息） （Fail Fast 方式）
        String role = ThreadLocalUtil.getRole();
        Long currentUserId = ThreadLocalUtil.getUserId();
        Long updateUserId = userDTO.getUserId();

        boolean isAdmin = RoleEnum.ADMIN.getRole().equals(role);
        boolean isSelf = updateUserId.equals(currentUserId);

        // 不满足任一合法条件：管理员 OR 本人
        if (!isAdmin && !isSelf) {
            return Result.error(MessageConstant.NOT_PERMISSION_UPDATE);
        }

        // 1.1 查询 名字 是否被占用
        User userByUsername = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, userDTO.getUsername()));
        if (userByUsername != null && !userByUsername.getUserId().equals(userId)) {
            return Result.error(MessageConstant.USERNAME + MessageConstant.ALREADY_EXISTS);
        }
        // 1.2 查询 电话 是否被占用
        User userByPhone = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, userDTO.getPhone()));
        if (userByPhone != null && !userByPhone.getUserId().equals(userId)) {
            return Result.error(MessageConstant.PHONE + MessageConstant.ALREADY_EXISTS);
        }
        // 1.3 查询 邮箱 是否被占用
        User userByEmail = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, userDTO.getEmail()));
        if (userByEmail != null && !userByEmail.getUserId().equals(userId)) {
            return Result.error(MessageConstant.EMAIL + MessageConstant.ALREADY_EXISTS);
        }

        // 2. 补全信息
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        user.setUpdateTime(LocalDateTime.now());

        // 3. 插入修改结果
        if (userMapper.updateById(user) == 0) {
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    /**
     * @Description: 更新用户状态
     * @Author: Kay
     * @date:   2025/11/17 20:35
     */
    @Override
    @CacheEvict(cacheNames = "userCache", allEntries = true)
    public Result updateUserStatus(Long userId, Integer userStatus) {
        // 1. 确保用户状态有效 , 并封装成 枚举类型
        UserStatusEnum statusEnum;
        if (userStatus == 0) {
            statusEnum = UserStatusEnum.ENABLE;
        } else if (userStatus == 1) {
            statusEnum = UserStatusEnum.DISABLE;
        } else {
            return Result.error(MessageConstant.USER_STATUS_INVALID);
        }

        // 2. 更新用户状态
        User user = new User();
        user.setUserStatus(statusEnum).setUpdateTime(LocalDateTime.now());

        int rows = userMapper.update(user, new LambdaQueryWrapper<User>().eq(User::getUserId, userId));
        if (rows == 0) {
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    /**
     * @Description: 删除用户
     * @Author: Kay
     * @date:   2025/11/17 20:43
     */
    @Override
    @CacheEvict(cacheNames = "userCache", allEntries = true)
    public Result deleteUser(Long userId) {
        if (userMapper.deleteById(userId) == 0) {
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }

    /**
     * @Description: 批量删除用户
     * @Author: Kay
     * @date:   2025/11/17 20:46
     */
    @Override
    @CacheEvict(cacheNames = "userCache", allEntries = true)
    public Result deleteUsers(List<Long> userIds) {
        if (userMapper.deleteByIds(userIds) == 0) {
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }

    /**
     * @Description: 发送验证码
     * @Author: Kay
     * @date:   2025/11/17 21:04
     */
    @Override
    public Result sendVerificationCode(String email) {
        // TODO 后期可以加一个判断逻辑，如果 redis 有对应的邮箱验证码，且还未过一分钟，则不能请求
        String verificationCode = emailService.sendVerificationCodeEmail(email);
        if (verificationCode == null) {
            return Result.error(MessageConstant.EMAIL_SEND_FAILED);
        }
        // 将验证码存储到Redis中，设置过期时间为5分钟
        stringRedisTemplate.opsForValue().set("verificationCode:" + email, verificationCode, 5, TimeUnit.MINUTES);
        return Result.success(MessageConstant.EMAIL_SEND_SUCCESS);
    }

    /**
     * @Description: 验证验证码
     * @param: email
     * @param: verificationCode
     * @Author: Kay
     * @date:   2025/11/18 23:45
     */
    @Override
    public boolean verifyVerificationCode(String email, String verificationCode) {
        String storedCode = stringRedisTemplate.opsForValue().get("verificationCode:" + email);
        return storedCode != null && storedCode.equals(verificationCode);
    }

    /**
     * @Description: 用户注册
     * @Author: Kay
     * @date:   2025/11/18 23:48
     */
    @Override
    @CacheEvict(cacheNames = "userCache", allEntries = true)
    public Result register(UserRegisterDTO userRegisterDTO) {
        // 能到这里， 说明验证码验证完了，并且是正确的
        // 1. 删除 redis 存的 验证码
        stringRedisTemplate.delete("verificationCode:" + userRegisterDTO.getEmail());

        // 2. 判断能否插入 -- username
        User userByUsername = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, userRegisterDTO.getUsername()));
        if (userByUsername != null) {
            return Result.error(MessageConstant.USERNAME + MessageConstant.ALREADY_EXISTS);
        }
        // 2. 判断能否插入 -- email
        User userByEmail = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, userRegisterDTO.getEmail()));
        if (userByEmail != null) {
            return Result.error(MessageConstant.EMAIL + MessageConstant.ALREADY_EXISTS);
        }
        // 3. 获取加密后的密码
        String passwordMD5 = DigestUtils.md5DigestAsHex(userRegisterDTO.getPassword().getBytes());
        // 4. 封装 需要插入的 User
        User user = new User().setUsername(userRegisterDTO.getUsername())
                .setPassword(passwordMD5)
                .setEmail(userRegisterDTO.getEmail())
                .setCreateTime(LocalDateTime.now())
                .setUpdateTime(LocalDateTime.now())
                .setUserStatus(UserStatusEnum.ENABLE);
        // 5. 插入数据
        if (userMapper.insert(user) == 0) {
            return Result.error(MessageConstant.REGISTER + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.REGISTER + MessageConstant.SUCCESS);
    }

    /**
     * @Description: 用户登录
     * @Author: Kay
     * @date:   2025/11/19 11:28
     */
    @Override
    public Result login(UserLoginDTO userLoginDTO) {
        // 1. 获取 匹配用户邮箱 的 用户
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, userLoginDTO.getEmail()));
        // 2.1 判断是否存在
        if ( user == null ) {
            return Result.error(MessageConstant.EMAIL + MessageConstant.ERROR);
        }
        // 2.2 判断是否封禁
        if (user.getUserStatus() != UserStatusEnum.ENABLE) {
            return Result.error(MessageConstant.ACCOUNT_LOCKED);
        }
        // 3. 匹配密码
        if (DigestUtils.md5DigestAsHex(userLoginDTO.getPassword().getBytes()).equals(user.getPassword())) {
            // 3.1 登录成功 ， 存放 token
            Map<String, Object> claims = new HashMap<>();
            claims.put(JwtClaimsConstant.ROLE, RoleEnum.USER.getRole());
            claims.put(JwtClaimsConstant.USER_ID, user.getUserId());
            claims.put(JwtClaimsConstant.USERNAME, user.getUsername());
            claims.put(JwtClaimsConstant.EMAIL, user.getEmail());
            String token = jwtUtil.generateToken(claims);

            // 3.2 将token存入redis
            stringRedisTemplate.opsForValue().set(token, token, 6, TimeUnit.HOURS);

            // 3.3 返回结果
            return Result.success(MessageConstant.LOGIN + MessageConstant.SUCCESS, token);
        }
        // 4. 登录失败
        return Result.error(MessageConstant.PASSWORD + MessageConstant.ERROR);
    }

    /**
     * @Description: 用户信息
     * @Author: Kay
     * @date:   2025/11/19 11:42
     */
    @Override
    public Result<UserVO> userInfo() {
        Long userId = ThreadLocalUtil.getUserId();
        User user = userMapper.selectById(userId);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return Result.success(userVO);
    }


}
