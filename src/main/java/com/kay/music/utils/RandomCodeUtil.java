package com.kay.music.utils;

import java.util.Random;

/**
 * @author Kay
 * @date 2025/11/17 21:08
 */
public class RandomCodeUtil {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final String CHARACTERS_NUMBER = "0123456789";
    private static final int LENGTH = 6; // 生成的字符串长度
    private static final Random random = new Random();

    /**
     * 生成随机的6个字符的字符串
     *
     * @return 随机字符串
     */
    public static String generateRandomCode() {
        StringBuilder stringBuilder = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            int index = random.nextInt(CHARACTERS_NUMBER.length());
            stringBuilder.append(CHARACTERS_NUMBER.charAt(index));
        }
        return stringBuilder.toString();
    }
}
