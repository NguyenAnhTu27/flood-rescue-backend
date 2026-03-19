package com.floodrescue.module.auth.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class PasswordResetSettings {

    private final int expiryMinutes;
    private final String emailFrom;

    public PasswordResetSettings(
            @Value("${app.auth.password-reset.expiry-minutes:15}") int expiryMinutes,
            @Value("${app.auth.password-reset.email-from:${spring.mail.username:}}") String emailFrom
    ) {
        this.expiryMinutes = Math.max(expiryMinutes, 1);
        this.emailFrom = emailFrom == null ? "" : emailFrom.trim();
    }
}