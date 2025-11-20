package com.kay.music.utils;

import com.kay.music.constant.JwtClaimsConstant;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Description: ThreadLocal 工具类
 * @Author: Kay
 * @date:   2025/11/16 17:05
 */
@Component
@RequiredArgsConstructor
public class ThreadLocalUtil {

    private final JwtUtil jwtUtil;

    // 提供ThreadLocal对象,
    private static final ThreadLocal THREAD_LOCAL = new ThreadLocal();

    // 根据键获取值
    public static <T> T get() {
        return (T) THREAD_LOCAL.get();
    }

    /**
     * @Description: 根据 token 重新设置 ThreadLocal ， 可以避免 某些被 拦截器 排除的接口 没有经过 jwt 设置 ThreadLocal
     *               如果 ThreadLocal 为空，但 header 里有 token，可以手动解析一次
     * @param: request
     * @return: void
     * @Author: Kay
     * @date:   2025/11/20 23:27
     */
    public void setThreadLocalByToken(HttpServletRequest request){
        if (ThreadLocalUtil.getUserId() == null) {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            if (token != null && !token.isEmpty()) {
                // 手动解析 JWT（直接用你已有的 JwtUtil）
                Map<String, Object> claims = jwtUtil.parseToken(token);
                ThreadLocalUtil.set(claims);
            }
        }
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
