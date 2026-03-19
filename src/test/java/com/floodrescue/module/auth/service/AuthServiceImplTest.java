package com.floodrescue.module.auth.service;

import com.floodrescue.config.security.JwtTokenProvider;
import com.floodrescue.module.auth.config.PasswordResetSettings;
import com.floodrescue.module.auth.dto.request.ForgotPasswordRequest;
import com.floodrescue.module.auth.dto.request.ResetPasswordRequest;
import com.floodrescue.module.auth.dto.response.ForgotPasswordResponse;
import com.floodrescue.module.auth.entity.AuthRefreshTokenEntity;
import com.floodrescue.module.auth.entity.PasswordResetTokenEntity;
import com.floodrescue.module.auth.repository.AuthRefreshTokenRepository;
import com.floodrescue.module.auth.repository.PasswordResetTokenRepository;
import com.floodrescue.module.user.entity.UserEntity;
import com.floodrescue.module.user.repository.RoleRepository;
import com.floodrescue.module.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private RoleRepository roleRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthRefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordResetEmailService passwordResetEmailService;

    @Mock
    private PasswordResetSettings passwordResetSettings;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void forgotPasswordShouldGenerateOtpAndSendEmail() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setIdentifier("citizen@example.com");

        UserEntity user = UserEntity.builder()
                .id(10L)
                .fullName("Citizen User")
                .email("citizen@example.com")
                .build();

        PasswordResetTokenEntity activeToken = PasswordResetTokenEntity.builder()
                .id(1L)
                .user(user)
                .tokenHash("old-hash")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .createdAt(LocalDateTime.now().minusMinutes(1))
                .build();

        when(userRepo.findByEmail("citizen@example.com")).thenReturn(Optional.of(user));
        when(passwordResetSettings.getExpiryMinutes()).thenReturn(15);
        when(passwordResetTokenRepository.findAllByUserIdAndUsedFalse(10L)).thenReturn(List.of(activeToken));
        when(passwordResetTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        ForgotPasswordResponse response = authService.forgotPassword(request);

        ArgumentCaptor<PasswordResetTokenEntity> tokenCaptor = ArgumentCaptor.forClass(PasswordResetTokenEntity.class);
        verify(passwordResetTokenRepository).save(tokenCaptor.capture());

        PasswordResetTokenEntity savedToken = tokenCaptor.getValue();
        assertThat(savedToken.getUser()).isSameAs(user);
        assertThat(savedToken.getUsed()).isFalse();
        assertThat(savedToken.getExpiresAt()).isAfter(LocalDateTime.now().plusMinutes(14));

        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(passwordResetEmailService).sendResetCode(
                anyString(),
                anyString(),
                codeCaptor.capture(),
                any(Integer.class)
        );

        assertThat(codeCaptor.getValue()).matches("\\d{6}");
        assertThat(response.getMaskedEmail()).contains("@");
        assertThat(response.getExpiresInMinutes()).isEqualTo(15);
        assertThat(response.getMessage()).isEqualTo("Nếu tài khoản tồn tại, mã xác nhận đã được gửi về email");

        verify(passwordResetTokenRepository).saveAll(List.of(activeToken));
        assertThat(activeToken.getUsed()).isTrue();
        assertThat(activeToken.getUsedAt()).isNotNull();
    }

    @Test
    void forgotPasswordShouldReturnGenericResponseWhenUserDoesNotExist() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setIdentifier("missing@example.com");

        when(userRepo.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        ForgotPasswordResponse response = authService.forgotPassword(request);

        assertThat(response.getMaskedEmail()).isNull();
        assertThat(response.getMessage()).isEqualTo("Nếu tài khoản tồn tại, mã xác nhận đã được gửi về email");
        verify(passwordResetTokenRepository, never()).save(any());
        verify(passwordResetEmailService, never()).sendResetCode(anyString(), anyString(), anyString(), any(Integer.class));
    }

    @Test
    void resetPasswordShouldAcceptEmailAndCode() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("citizen@example.com");
        request.setCode("123456");
        request.setNewPassword("NewPass1");

        UserEntity user = UserEntity.builder()
                .id(20L)
                .email("citizen@example.com")
                .passwordHash("old-password")
                .build();

        PasswordResetTokenEntity resetToken = PasswordResetTokenEntity.builder()
                .id(3L)
                .user(user)
                .tokenHash(hashResetCode("citizen@example.com", "123456"))
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .used(false)
                .createdAt(LocalDateTime.now().minusMinutes(1))
                .build();

        AuthRefreshTokenEntity refreshToken = AuthRefreshTokenEntity.builder()
                .id(2L)
                .user(user)
                .revoked(false)
                .build();

        when(passwordResetTokenRepository.findByTokenHash(hashResetCode("citizen@example.com", "123456")))
                .thenReturn(Optional.of(resetToken));
        when(passwordEncoder.encode("NewPass1")).thenReturn("encoded-password");
        when(refreshTokenRepository.findByUserIdAndRevokedFalse(20L)).thenReturn(List.of(refreshToken));

        authService.resetPassword(request);

        assertThat(user.getPasswordHash()).isEqualTo("encoded-password");
        assertThat(resetToken.getUsed()).isTrue();
        assertThat(resetToken.getUsedAt()).isNotNull();
        assertThat(refreshToken.getRevoked()).isTrue();
        assertThat(refreshToken.getRevokedAt()).isNotNull();

        verify(userRepo).save(user);
        verify(passwordResetTokenRepository).save(resetToken);
        verify(refreshTokenRepository).saveAll(List.of(refreshToken));
    }

    private String hashResetCode(String email, String code) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest((email + ":" + code).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}