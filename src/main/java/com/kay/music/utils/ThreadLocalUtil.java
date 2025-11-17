package com.kay.music.utils;

import com.kay.music.constant.JwtClaimsConstant;

import java.util.Map;

/**
 * @Description: ThreadLocal 工具类
 * @Author: Kay
 * @date:   2025/11/16 17:05
 */
public class ThreadLocalUtil {

    // 提供ThreadLocal对象,
    private static final ThreadLocal THREAD_LOCAL = new ThreadLocal();

    // 根据键获取值
    public static <T> T get() {
        return (T) THREAD_LOCAL.get();
    }

    /**
     * @Description: 获取 Admin ID
     * @return: Long Admin ID
     * @Author: Kay
     * @date:   2025/11/17 20:15
     */
    public static Long getAdminId() {
        Map<String, Object> claims = (Map<String, Object>) THREAD_LOCAL.get();
        if (claims != null) {
            return (Long) claims.get(JwtClaimsConstant.ADMIN_ID);
        }
        return null;
    }

    /**
     * @Description: 获取 User ID
     * @return: Long User ID
     * @Author: Kay
     * @date:   2025/11/17 20:15
     */
    public static Long getUserId() {
        Map<String, Object> claims = (Map<String, Object>) THREAD_LOCAL.get();
        if (claims != null) {
            return (Long) claims.get(JwtClaimsConstant.USER_ID);  // 假设 JwtClaimsConstant 有 USER_ID 常量
        }
        return null;
    }

    /**
     * @Description: 获取用户角色
     * @Author: Kay
     * @date:   2025/11/17 20:21
     */
    public static String getRole() {
        Map<String, Object> claims = (Map<String, Object>) THREAD_LOCAL.get();
        if (claims == null) {
            return null;
        }
        return (String) claims.get(JwtClaimsConstant.ROLE);
    }

    // 存储键值对
    public static void set(Object value) {
        THREAD_LOCAL.set(value);
    }

    // 清除ThreadLocal 防止内存泄漏
    public static void remove() {
        THREAD_LOCAL.remove();
    }
}
