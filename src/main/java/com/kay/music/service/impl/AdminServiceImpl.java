package com.kay.music.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kay.music.constant.JwtClaimsConstant;
import com.kay.music.constant.MessageConstant;
import com.kay.music.enumeration.RoleEnum;
import com.kay.music.mapper.AdminMapper;
import com.kay.music.pojo.dto.AdminDTO;
import com.kay.music.pojo.entity.Admin;
import com.kay.music.result.Result;
import com.kay.music.service.IAdminService;
import com.kay.music.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * @author Kay
 * @date 2025/11/16 13:05
 */
@Service
@RequiredArgsConstructor
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements IAdminService {

    private final AdminMapper adminMapper;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * @Description: 管理员注册
     * @Author: Kay
     * @date:   2025/11/16 13:09
     */
    @Override
    public Result register(AdminDTO adminDTO) {
        // 1. 检查是否已经注册
        Admin admin = adminMapper.selectOne(
                new LambdaQueryWrapper<Admin>()
                        .eq(Admin::getUsername, adminDTO.getUsername())
        );
        if ( admin != null ) {
            return Result.error(MessageConstant.USERNAME + MessageConstant.ALREADY_EXISTS);
        }
        // 2. 对密码进行加密 ,MD5 加密后得到 32 位十六进制字符串
        String passwordMD5 = DigestUtils.md5DigestAsHex(adminDTO.getPassword().getBytes());
        // 3. 组装成新的 Admin
        Admin adminRegister = new Admin()
                .setUsername(adminDTO.getUsername())
                .setPassword(passwordMD5);
        // 4. 插入数据
        int insertNum = adminMapper.insert(adminRegister);
        // 5. 返回结果
        if ( insertNum == 0 ) {
            return Result.error(MessageConstant.REGISTER + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.REGISTER + MessageConstant.SUCCESS);
    }

    /**
     * @Author: Kay
     * @date:   2025/11/16 16:24
     */
    @Override
    public Result login(AdminDTO adminDTO) {
        // 1. 查询 管理员账号是否存在
        Admin admin = adminMapper.selectOne(
                new LambdaQueryWrapper<Admin>()
                        .eq(Admin::getUsername, adminDTO.getUsername())
        );
        if ( admin == null ) {
            return Result.error(MessageConstant.USERNAME + MessageConstant.NOT_EXIST);
        }

        if ( DigestUtils.md5DigestAsHex(adminDTO.getPassword().getBytes()).equals(admin.getPassword()) ) {
            // 2.1 登录 成功 ， 准备 claims
            Map<String,Object> claims = new HashMap<>();
            claims.put(JwtClaimsConstant.ROLE, RoleEnum.ADMIN.getRole());
            claims.put(JwtClaimsConstant.ADMIN_ID , admin.getAdminId());
            claims.put(JwtClaimsConstant.USERNAME , admin.getUsername());
            String token = JwtUtil.generateToken(claims);

            // 2.2 将 token 存入 redis
            stringRedisTemplate.opsForValue().set(token, token , 6 , TimeUnit.HOURS);

            // 2.3 返回
            return Result.success(MessageConstant.LOGIN + MessageConstant.SUCCESS , token);
        }
        return Result.error(MessageConstant.PASSWORD + MessageConstant.ERROR);
    }

    /**
     * @Author: Kay
     * @date:   2025/11/16 16:59
     */
    @Override
    public Result logout(String token) {
        // 注销 token
        Boolean res = stringRedisTemplate.delete(token);
        if ( res != null && res ) {
            return Result.success(MessageConstant.LOGOUT + MessageConstant.SUCCESS);
        }
        return Result.error(MessageConstant.LOGOUT + MessageConstant.FAILED);
    }
}
