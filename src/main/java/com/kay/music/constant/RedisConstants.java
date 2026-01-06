package com.kay.music.constant;

/**
 * Redis 常量类
 *
 * @author Kay
 * @date 2026/01/06 21:08
 */
public class RedisConstants {

    public static final String PREFIX = "music:";
    /**
     * 登录用户 Redis key 前缀
     */
    public static final String LOGIN_USER_KEY = "music:user:token:";
    
    /**
     * 登录用户 token 有效期（小时）
     */
    public static final Long LOGIN_USER_TTL = 36L;
    
    /**
     * 歌曲缓存key
     */
    public static final String CACHE_SONG_KEY = "music:song:";
    
    /**
     * 歌曲数据的缓存有效期（分钟）
     */
    public static final Long CACHE_SONG_TTL = 30L;
    
    /**
     * 歌单数据 Redis key 前缀
     */
    public static final String CACHE_PLAYLIST_KEY = "music:playlist:";
    
    /**
     * 歌单数据的缓存有效期（分钟）
     */
    public static final Long CACHE_PLAYLIST_TTL = 30L;
    
    /**
     * 艺术家数据 Redis key 前缀
     */
    public static final String CACHE_ARTIST_KEY = "music:artist:";
    
    /**
     * 艺术家数据的缓存有效期（分钟）
     */
    public static final Long CACHE_ARTIST_TTL = 60L;
    
    /**
     * 验证码 Redis key 前缀
     */
    public static final String VERIFICATION_CODE_KEY = "music:verify:code:";
    
    /**
     * 验证码的有效期（分钟）
     */
    public static final Long VERIFICATION_CODE_TTL = 5L;
    
    /**
     * 空值缓存的有效期（分钟）
     */
    public static final Long CACHE_NULL_TTL = 2L;
    
    /**
     * 锁前缀
     */
    public static final String LOCK_KEY = "music:lock:";
    
    /**
     * 互斥锁的有效期（秒）
     */
    public static final Long LOCK_TTL = 10L;
}
