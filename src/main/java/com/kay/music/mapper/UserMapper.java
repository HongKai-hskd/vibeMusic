package com.kay.music.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kay.music.pojo.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Kay
 * @date 2025/11/17 18:05
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
