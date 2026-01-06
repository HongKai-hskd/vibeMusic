package com.kay.music.pojo.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * Redis逻辑过期数据包装类
 * 用于缓存击穿问题的解决方案
 *
 * @author Kay
 * @date 2026/01/06 20:51
 */
@Data
@Accessors(chain = true)
public class RedisData<T> {
    /**
     * 逻辑过期时间
     */
    private LocalDateTime expireTime;
    
    /**
     * 实际数据
     */
    private T data;
}
