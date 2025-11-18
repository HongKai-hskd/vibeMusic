package com.kay.music.service.impl;

import com.kay.music.constant.MessageConstant;
import com.kay.music.service.EmailService;
import com.kay.music.utils.RandomCodeUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * @author Kay
 * @date 2025/11/17 21:05
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    /**
     * @Description: 发送邮件
     * @param: to        收件人地址
     * @param: subject   邮件主题
     * @param: content   邮件内容
     * @return: boolean  发送结果，包含是否成功
     * @Author: Kay
     * @date:   2025/11/17 21:11
     */
    public boolean sendEmail(String to, String subject, String content) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content);
            mailSender.send(mimeMessage);
            return true;
        } catch (MessagingException e) {
            log.error(MessageConstant.EMAIL_SEND_FAILED, e);
            return false;
        }
    }

    /**
     * @Description: 发送验证码邮件
     * @param: email 收件人地址
     * @return: String 发送结果，包含是否成功和验证码
     * @Author: Kay
     * @date:   2025/11/17 21:07
     */
    @Override
    public String sendVerificationCodeEmail(String email) {
        String verificationCode = RandomCodeUtil.generateRandomCode();
        String subject = "【Vibe Music】验证码";
        String content = "您的验证码为：" + verificationCode + "，验证码于五分钟后过期。";
        boolean success = sendEmail(email, subject, content);
        if (success) {
            return verificationCode;
        } else {
            return null;
        }
    }
}
