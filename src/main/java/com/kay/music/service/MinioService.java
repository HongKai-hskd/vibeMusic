package com.kay.music.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author Kay
 * @date 2025/11/19 21:09
 */
public interface MinioService {

    String uploadFile(MultipartFile file, String folder);

    void deleteFile(String userAvatar);
}
