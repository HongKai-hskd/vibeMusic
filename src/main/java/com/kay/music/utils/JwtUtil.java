package com.kay.music.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * @Description: JWT 工具类
 * @Author: Kay
 * @date:   2025/11/16 16:38
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret_key}")
    private String SECRET_KEY;

    @Value("${jwt.expiration_time}")
    private Long EXPIRATION_HOUR;

    /**
     * 生成 JWT token
     *
     * @param claims 自定义的业务数据
     * @return JWT token
     */
    public String generateToken(Map<String, Object> claims) {
        long expireMillis = EXPIRATION_HOUR * 60 * 60 * 1000;

        return JWT.create()
                .withClaim("claims", claims) // 自定义的业务数据
                .withExpiresAt(new Date(System.currentTimeMillis() + expireMillis)) // 设置过期时间
                .sign(Algorithm.HMAC256(SECRET_KEY)); // 使用 HMAC256 算法加密
    }

    /**
     * 解析 JWT token
     *
     * @param token JWT token
     * @return 自定义的业务数据
     */
    public Map<String, Object> parseToken(String token) {
        return JWT.require(Algorithm.HMAC256(SECRET_KEY))
                .build()
                .verify(token)
                .getClaim("claims")
                .asMap();
    }

}
