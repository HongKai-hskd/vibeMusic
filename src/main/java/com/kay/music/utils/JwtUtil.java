package com.kay.music.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;

import java.util.Date;
import java.util.Map;

/**
 * @Description: JWT 工具类
 * @Author: Kay
 * @date:   2025/11/16 16:38
 */
public class JwtUtil {

    @Value("${jwt.secret_key}")
    private String secretKey;

    @Value("${jwt.expiration_time}")
    private Long expirationHour;

    private static String SECRET_KEY;
    private static Long EXPIRATION_TIME;

    @PostConstruct
    public void init() {
        SECRET_KEY = secretKey;
        EXPIRATION_TIME = expirationHour * 60 * 60 * 1000;
    }

    /**
     * 生成 JWT token
     *
     * @param claims 自定义的业务数据
     * @return JWT token
     */
    public static String generateToken(Map<String, Object> claims) {
        return JWT.create()
                .withClaim("claims", claims) // 自定义的业务数据
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // 设置过期时间
                .sign(Algorithm.HMAC256(SECRET_KEY)); // 使用 HMAC256 算法加密
    }

    /**
     * 解析 JWT token
     *
     * @param token JWT token
     * @return 自定义的业务数据
     */
    public static Map<String, Object> parseToken(String token) {
        return JWT.require(Algorithm.HMAC256(SECRET_KEY))
                .build()
                .verify(token)
                .getClaim("claims")
                .asMap();
    }

}
