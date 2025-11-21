package com.kay.music.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Description: 解决跨域问题
 * @author Kay
 * @date 2025/11/21 10:45
 */
// 在不同域名/端口的情况，正常调用你的后端接口，并且还能带上 Cookie / Token，还能在前端代码里拿到响应头里的 Authorization
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NotNull CorsRegistry registry) {
                registry.addMapping("/**") // 允许所有路径
                        .allowedOriginPatterns("*") // 允许所有来源（推荐）
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS") // 加上 OPTIONS
                        .allowedHeaders("*") // 允许所有请求头
                        .exposedHeaders("Authorization") // 允许前端获取 Authorization 头
                        .allowCredentials(true); // 允许携带 Cookie 或 Token
            }
        };
    }
}
