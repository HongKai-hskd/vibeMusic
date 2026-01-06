package com.kay.music.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.kay.music.pojo.dto.RedisData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 缓存客户端工具类
 * 用于解决缓存穿透、缓存击穿问题
 *
 * @author Kay
 * @date 2026/01/06 20:52
 */
@Slf4j
@Component
public class CacheClient {
    
    private final StringRedisTemplate stringRedisTemplate;
    
    /**
     * 线程池，用于异步更新缓存
     */
    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    public CacheClient(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 将任意Java对象序列化为JSON并存储在Redis中，并设置TTL过期时间
     */
    public void set(String key, Object value, Long time, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, unit);
    }

    /**
     * 将任意Java对象序列化为JSON并存储在Redis中，并设置逻辑过期时间
     * 用于解决缓存击穿问题
     */
    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit) {
        // 设置逻辑过期时间
        RedisData<Object> redisData = new RedisData<>();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        
        // 写入Redis，不设置TTL过期时间
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    /**
     * 根据指定的key查询缓存，并反序列化为指定类型，带有缓存穿透解决方案
     * @param keyPrefix 键前缀
     * @param id 主键
     * @param type 返回值类型
     * @param dbFallback 查询数据库的函数
     * @param time 缓存时间
     * @param unit 时间单位
     * @return 查询结果
     * @param <R> 返回值类型
     * @param <ID> 主键类型
     */
    public <R, ID> R queryWithPassThrough(
            String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        // 1. 从Redis查询商品缓存
        String json = stringRedisTemplate.opsForValue().get(key);
        
        // 2. 判断是否存在
        if (StrUtil.isNotBlank(json)) {
            // 3. 存在，直接返回
            return JSONUtil.toBean(json, type);
        }
        
        // 判断命中的是否是空值
        if (json != null) {
            // 返回一个错误信息或空对象
            return null;
        }

        // 4. 不存在，根据id查询数据库
        R r = dbFallback.apply(id);
        
        // 5. 数据库中也不存在，返回错误
        if (r == null) {
            // 将空值写入Redis
            stringRedisTemplate.opsForValue().set(key, "", time, unit);
            // 返回错误信息
            return null;
        }
        
        // 6. 存在，写入Redis
        this.set(key, r, time, unit);
        
        // 7. 返回
        return r;
    }

    /**
     * 根据指定的key查询缓存，并反序列化为指定类型，带有逻辑过期解决缓存击穿问题的方案
     * @param keyPrefix 键前缀
     * @param id 主键
     * @param type 返回值类型
     * @param dbFallback 查询数据库的函数
     * @param time 缓存时间
     * @param unit 时间单位
     * @return 查询结果
     * @param <R> 返回值类型
     * @param <ID> 主键类型
     */
    public <R, ID> R queryWithLogicalExpire(
            String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        // 1. 从Redis查询缓存
        String json = stringRedisTemplate.opsForValue().get(key);
        
        // 2. 判断是否存在
        if (StrUtil.isBlank(json)) {
            // 3. 未命中，返回null
            return null;
        }
        
        // 4. 命中，需要先把json反序列化为对象
        RedisData<R> redisData = JSONUtil.toBean(json, RedisData.class);
        R r = JSONUtil.toBean((JSONObject) redisData.getData(), type);
        LocalDateTime expireTime = redisData.getExpireTime();
        
        // 5. 判断是否过期
        if (expireTime.isAfter(LocalDateTime.now())) {
            // 5.1 未过期，直接返回
            return r;
        }
        
        // 5.2 已过期，需要缓存重建
        // 6. 缓存重建
        // 6.1 获取互斥锁
        String lockKey = "lock:" + keyPrefix + id;
        boolean isLock = tryLock(lockKey);
        
        // 6.2 判断是否获取锁成功
        if (isLock) {
            // 6.3 成功，开启独立线程，实现缓存重建
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    // 查询数据库
                    R newR = dbFallback.apply(id);
                    // 重建缓存
                    this.setWithLogicalExpire(key, newR, time, unit);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    // 释放锁
                    unlock(lockKey);
                }
            });
        }
        
        // 6.4 返回过期的商品信息
        return r;
    }

    /**
     * 根据指定的key查询缓存，并反序列化为指定类型，带有互斥锁解决缓存击穿问题的方案
     * @param keyPrefix 键前缀
     * @param id 主键
     * @param type 返回值类型
     * @param dbFallback 查询数据库的函数
     * @param time 缓存时间
     * @param unit 时间单位
     * @return 查询结果
     * @param <R> 返回值类型
     * @param <ID> 主键类型
     */
    public <R, ID> R queryWithMutex(
            String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        // 1. 从Redis查询缓存
        String json = stringRedisTemplate.opsForValue().get(key);
        
        // 2. 判断是否存在
        if (StrUtil.isNotBlank(json)) {
            // 3. 存在，直接返回
            return JSONUtil.toBean(json, type);
        }
        
        // 判断命中的是否是空值
        if (json != null) {
            // 返回一个错误信息或空对象
            return null;
        }

        // 4. 实现缓存重建
        // 4.1 获取互斥锁
        String lockKey = "lock:" + keyPrefix + id;
        R r = null;
        try {
            boolean isLock = tryLock(lockKey);
            // 4.2 判断是否获取成功
            if (!isLock) {
                // 4.3 获取锁失败，休眠并重试
                Thread.sleep(50);
                return queryWithMutex(keyPrefix, id, type, dbFallback, time, unit);
            }
            
            // 4.4 获取锁成功，根据id查询数据库
            r = dbFallback.apply(id);
            
            // 模拟重建的延时
            Thread.sleep(200);
            
            // 5. 数据库中也不存在，返回错误
            if (r == null) {
                // 将空值写入Redis
                stringRedisTemplate.opsForValue().set(key, "", time, unit);
                // 返回错误信息
                return null;
            }
            
            // 6. 存在，写入Redis
            this.set(key, r, time, unit);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // 7. 释放锁
            unlock(lockKey);
        }
        
        // 8. 返回
        return r;
    }

    /**
     * 尝试获取锁
     * @param key 锁的key
     * @return 是否获取成功
     */
    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    /**
     * 释放锁
     * @param key 锁的key
     */
    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }
}
