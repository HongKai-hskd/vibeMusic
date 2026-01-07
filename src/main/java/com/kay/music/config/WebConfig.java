package com.kay.music.config;


import com.kay.music.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 登录接口和注册接口不拦截
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**") // 拦截所有请求
                .excludePathPatterns(
                        // ------------ knife4j--------------
                        "/doc.html",          //  Knife4j 首页
                        "/webjars/**",        //  静态资源（CSS/JS/图标）
                        "/v3/api-docs/**",    //  OpenAPI 3.0 接口数据
                        "/swagger-resources/**", //  Swagger 资源配置（兼容旧版本）
                        "/swagger-ui.html",   //  原生 Swagger 首页（备用）
                        "/knife4j/**",         //  Knife4j 增强功能（如调试回调）
                        // -------------------------------------
                        "/admin/login", "/admin/logout", "/admin/register",
                        "/user/login", "/user/logout", "/user/register",
                        "/user/sendVerificationCode", "/user/resetUserPassword",
                        "/banner/getBannerList",
                        "/playlist/getAllPlaylists", "/playlist/getRecommendedPlaylists", "/playlist/getPlaylistDetail/**",
                        "/artist/getAllArtists", "/artist/getArtistDetail/**", "/artist/getRandomArtists",
                        "/song/getAllSongs", "/song/getRecommendedSongs", "/song/getSongDetail/**");
    }
}
