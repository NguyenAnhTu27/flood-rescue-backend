package com.floodrescue.module.auth.service;

import com.floodrescue.module.auth.config.PasswordResetSettings;
import com.floodrescue.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordResetEmailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final PasswordResetSettings passwordResetSettings;

    public void sendResetCode(String email, String fullName, String code, int expiryMinutes) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            throw new BusinessException("Chức năng gửi email chưa được cấu hình");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        if (!passwordResetSettings.getEmailFrom().isBlank()) {
            message.setFrom(passwordResetSettings.getEmailFrom());
        }
        message.setTo(email);
        message.setSubject("Mã xác nhận đặt lại mật khẩu Flood Rescue");
        message.setText(buildBody(fullName, code, expiryMinutes));

        try {
            mailSender.send(message);
        } catch (MailException ex) {
            throw new BusinessException("Không thể gửi mã xác nhận qua email. Vui lòng thử lại sau");
        }
    }

    private String buildBody(String fullName, String code, int expiryMinutes) {
        String displayName = fullName == null || fullName.isBlank() ? "ban" : fullName.trim();
        return "Xin chao " + displayName + ",\n\n"
                + "Ban vua yeu cau dat lai mat khau cho tai khoan Flood Rescue.\n"
                + "Ma xac nhan cua ban la: " + code + "\n"
                + "Ma nay co hieu luc trong " + expiryMinutes + " phut.\n\n"
                + "Neu ban khong thuc hien yeu cau nay, vui long bo qua email nay.";
    }
}