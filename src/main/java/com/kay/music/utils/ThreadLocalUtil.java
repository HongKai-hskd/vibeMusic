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
     * @Description: 通用：从 claims 中安全地拿一个 Long 类型的值
     * @param: key
     * @return: Long
     * @Author: Kay
     * @date:   2025/11/19 20:49
     */
    private static Long getLongClaim(String key) {
        Map<String, Object> claims = (Map<String, Object>) THREAD_LOCAL.get();
        if (claims == null) {
            return null;
        }

        Object value = claims.get(key);
        if (value == null) {
            return null;
        }

        // 关键：无论 Integer / Long / BigDecimal 都能转
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        // 如果未来 value 是 String 也能兼容
        try {
            return Long.parseLong(value.toString());
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Claim [" + key + "] 类型无法转换为 Long，实际类型：" + value.getClass());
        }
    }

    /**
     * @Description: 获取 Admin ID
     * @return: Long Admin ID
     * @Author: Kay
     * @date:   2025/11/17 20:15
     */
    public static Long getAdminId() {
        return getLongClaim(JwtClaimsConstant.ADMIN_ID);
    }

    /**
     * @Description: 获取 User ID
     * @return: Long User ID
     * @Author: Kay
     * @date:   2025/11/17 20:15
     */
    public static Long getUserId() {
        return getLongClaim(JwtClaimsConstant.USER_ID);
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
