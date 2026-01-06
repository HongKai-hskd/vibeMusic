package com.kay.music.config;

import com.kay.music.pojo.dto.SongDTO;
import com.kay.music.service.ISongService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 热点歌曲数据预热加载器
 * 系统启动时自动加载热点歌曲数据到缓存
 * 
 * @author Kay
 * @date 2026/01/06 21:14
 */
@Slf4j
@Component
public class HotDataPreloader implements InitializingBean {
    
    /**
     * 注入歌曲服务，用于日志记录
     */
    @Resource
    private ISongService songService;
    
    /**
     * 使用ApplicationContext来获取被代理的bean实例
     * 解决Spring缓存注解自调用问题
     */
    @Resource
    private ApplicationContext applicationContext;
    
    /**
     * 系统启动后，自动执行此方法，预热歌曲缓存
     * 
     * 【注愎】：使用代理对象调用来解决Spring缓存自调用问题
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("开始预热热点歌曲数据...");
        
        try {
            // 构建查询参数 {pageNum: 1, pageSize: 20}
            SongDTO songDTO = new SongDTO();
            songDTO.setPageNum(1);
            songDTO.setPageSize(20);
            
            // 获取歌曲服务的代理对象
            ISongService proxiedSongService = applicationContext.getBean(ISongService.class);
            
            // 预热歌曲列表
            log.info("开始预热歌曲列表...");
            proxiedSongService.getAllSongsForGuest(songDTO);  // 使用代理对象调用
            log.info("歌曲列表预热完成");
            
            log.info("热点歌曲数据预热成功");
        } catch (Exception e) {
            log.error("热点歌曲预热过程中出现异常", e);
        }
    }
}
