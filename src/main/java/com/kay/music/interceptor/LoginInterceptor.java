package com.kay.music.interceptor;



import com.kay.music.config.RolePermissionManager;
import com.kay.music.constant.JwtClaimsConstant;
import com.kay.music.constant.MessageConstant;
import com.kay.music.constant.PathConstant;
import com.kay.music.utils.JwtUtil;
import com.kay.music.utils.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
/**
 * @Description: 自定义的登录 + 权限拦截器。用 Redis 校验 token 是否有效，用 JWT 解析出角色，再根据配置文件里“角色 → 可访问路径”的映射，判断请求有没有权限。
 * @Author: Kay
 * @date:   2025/11/16 17:10
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate stringRedisTemplate;
    private final RolePermissionManager rolePermissionManager;
    private final JwtUtil jwtUtil;

    public void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8"); // 设置字符编码为UTF-8
        response.setContentType("application/json;charset=UTF-8"); // 设置响应的Content-Type
        response.getWriter().write(message);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 允许 CORS 预检请求（OPTIONS 方法）直接通过 ，避免预检请求也被当成需要登录、需要权限的普通请求拦截
        if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true; // 直接放行，确保 CORS 预检请求不会被拦截
        }
        // 2. 从请求头拿 token，并处理 Bearer 前缀
        String token = request.getHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // 去掉 "Bearer " 前缀
        }
        String path = request.getRequestURI();

        // 3. 匹配“无需登录也能访问”的公开接口
        // TODO 这里的 “无需登录也能访问”的公开接口 ， 和 WebConfig 中的 excludePathPatterns 感觉有些重复
        // 获取 Spring 的 PathMatcher 实例
        PathMatcher pathMatcher = new AntPathMatcher();

        // 定义允许访问的路径
        List<String> allowedPaths = Arrays.asList(
                PathConstant.PLAYLIST_DETAIL_PATH,
                PathConstant.ARTIST_DETAIL_PATH,
                PathConstant.SONG_LIST_PATH,
                PathConstant.SONG_DETAIL_PATH
        );

        // 检查路径是否匹配
        boolean isAllowedPath = allowedPaths.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));

        // 4. 如果没有 token：只允许访问公开接口
        if (token == null || token.isEmpty()) {
            if (isAllowedPath) {
                return true; // 允许未登录用户访问这些路径
            }

            sendErrorResponse(response, 401, MessageConstant.NOT_LOGIN); // 缺少令牌
            return false;
        }

        try {
            // 5. 校验 token 是否有效（Redis 校验）
            // 从redis中获取相同的token
            ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
            String redisToken;

            try {
                // 增加健壮性 ， 避免全部归为最后的异常处理
                redisToken = operations.get(token);
            } catch (RedisConnectionFailureException ex) {
                log.error("Redis 连接失败，无法校验 token", ex);
                sendErrorResponse(response, 500, "Redis 服务不可用，请联系管理员");
                return false;
            }

            if (redisToken == null) {
                // Redis 里根本没有这个 token，说明会话真的过期了
                sendErrorResponse(response, 401, MessageConstant.SESSION_EXPIRED);
                return false;
            }
            // 6.  解析 JWT，获取角色信息
            Map<String, Object> claims;
            try{
                claims = jwtUtil.parseToken(token);
            } catch (Exception ex) {
                // JWT 无效/过期等
                log.warn("JWT 解析失败，token 无效", ex);
                sendErrorResponse(response, 401, MessageConstant.SESSION_EXPIRED);
                return false;
            }
            String role = (String) claims.get(JwtClaimsConstant.ROLE);
            String requestURI = request.getRequestURI();

            // 7. 用 RolePermissionManager 做路径权限判断
            if (rolePermissionManager.hasPermission(role, requestURI)) {
                // 把业务数据存储到ThreadLocal中
                ThreadLocalUtil.set(claims);
                return true;
            } else {
                sendErrorResponse(response, 403, MessageConstant.NO_PERMISSION); // 无权限访问
                return false;
            }
        } catch (Exception e) {
            // 拦截器里其他未知异常，单独打个日志，返回服务器异常
            log.error("权限拦截器发生未知异常", e);
            sendErrorResponse(response, 401, MessageConstant.SESSION_EXPIRED); // 令牌无效
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清空ThreadLocal中的数据
        ThreadLocalUtil.remove();
    }
}
