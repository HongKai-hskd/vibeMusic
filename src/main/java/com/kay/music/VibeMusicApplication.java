package com.kay.music;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
@MapperScan("com.kay.music.mapper")
public class VibeMusicApplication {

    public static void main(String[] args) {
        SpringApplication.run(VibeMusicApplication.class, args);
        log.info("项目启动成功");
    }

}
