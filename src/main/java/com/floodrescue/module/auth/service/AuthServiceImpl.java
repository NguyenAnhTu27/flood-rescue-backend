package com.floodrescue.module.auth.service;

import com.floodrescue.config.security.JwtTokenProvider;
import com.floodrescue.module.auth.config.PasswordResetSettings;
import com.floodrescue.module.auth.dto.request.ForgotPasswordRequest;
import com.floodrescue.module.auth.dto.request.LoginRequest;
import com.floodrescue.module.auth.dto.request.RegisterCitizenRequest;
import com.floodrescue.module.auth.dto.request.ResetPasswordRequest;
import com.floodrescue.module.auth.dto.response.ForgotPasswordResponse;
import com.floodrescue.module.auth.dto.response.LoginResponse;
import com.floodrescue.module.auth.dto.response.UserProfileResponse;
import com.floodrescue.module.auth.entity.AuthRefreshTokenEntity;
import com.floodrescue.module.auth.entity.PasswordResetTokenEntity;
import com.floodrescue.module.auth.repository.AuthRefreshTokenRepository;
import com.floodrescue.module.auth.repository.PasswordResetTokenRepository;
import com.floodrescue.module.user.entity.RoleEntity;
import com.floodrescue.module.user.entity.UserEntity;
import com.floodrescue.module.user.repository.RoleRepository;
import com.floodrescue.module.user.repository.UserRepository;
import com.floodrescue.shared.exception.BusinessException;
import com.floodrescue.shared.exception.NotFoundException;
import com.floodrescue.shared.exception.UnauthorizedException;
import com.floodrescue.shared.util.PhoneUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthRefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordResetEmailService passwordResetEmailService;
    private final PasswordResetSettings passwordResetSettings;

    private static final int PASSWORD_RESET_CODE_LENGTH = 6;

    @Override
    public void registerCitizen(RegisterCitizenRequest req) {
        // Normalize and sanitize phone number
        String normalizedPhone = PhoneUtil.normalize(req.getPhone());
        if (normalizedPhone == null) {
            throw new BusinessException("Số điện thoại không hợp lệ");
        }

        // Check for duplicate phone (using normalized format)
        if (userRepo.existsByPhone(normalizedPhone)) {
            throw new BusinessException("Số điện thoại đã tồn tại");
        }

        // Normalize email if provided
        String normalizedEmail = null;
        if (req.getEmail() != null && !req.getEmail().isBlank()) {
            normalizedEmail = req.getEmail().trim().toLowerCase();
            if (userRepo.existsByEmail(normalizedEmail)) {
                throw new BusinessException("Email đã tồn tại");
            }
        }

        RoleEntity citizenRole = roleRepo.findByCode("CITIZEN")
                .orElseThrow(() -> new BusinessException("Chưa có role CITIZEN trong bảng roles"));

        LocalDateTime now = LocalDateTime.now();

        UserEntity user = UserEntity.builder()
                .role(citizenRole)
                .teamId(null)
                .fullName(req.getFullName().trim())
                .phone(normalizedPhone) // Store normalized phone
                .email(normalizedEmail) // Store normalized email
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .status((byte) 1)
                .createdAt(now)
                .updatedAt(now)
                .build();

        userRepo.save(user);
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest req) {
        String identifier = req.getIdentifier().trim();

        UserEntity user = null;

        // Try to normalize as phone number first
        String normalizedPhone = PhoneUtil.normalize(identifier);
        if (normalizedPhone != null) {
            // Search by normalized phone
            user = userRepo.findByPhone(normalizedPhone).orElse(null);
        }

        // If not found by phone, try email (normalized to lowercase)
        if (user == null) {
            String normalizedEmail = identifier.toLowerCase();
            user = userRepo.findByEmail(normalizedEmail).orElse(null);
        }

        if (user == null) {
            throw new UnauthorizedException("Tài khoản không tồn tại");
        }

        if (user.getStatus() == 0) {
            throw new UnauthorizedException("Tài khoản đã bị vô hiệu hóa");
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Mật khẩu không đúng");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepo.save(user);

        String roleCode = user.getRole().getCode();
        String token = jwtTokenProvider.generateToken(user.getId(), roleCode);
        String refreshToken = issueRefreshToken(user);

        return LoginResponse.builder()
                .token(token)
            .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .fullName(user.getFullName())
                .role(roleCode)
                .build();
    }

    private String generateToken(UserEntity user) {
        String roleCode = user.getRole() != null ? user.getRole().getCode() : "";
        return jwtTokenProvider.generateToken(user.getId(), roleCode);
    }

    @Override
    @Transactional
    public LoginResponse registerAndLogin(RegisterCitizenRequest req) {
        registerCitizen(req);
        // Reload user so we get the persisted entity with ID
        String normalizedPhone = PhoneUtil.normalize(req.getPhone());
        UserEntity user = userRepo.findByPhone(normalizedPhone)
                .orElseThrow(() -> new BusinessException("Không tìm thấy user sau khi đăng ký"));
        String token = generateToken(user);
        String refreshToken = issueRefreshToken(user);
        return LoginResponse.builder()
            .message("Đăng ký Citizen thành công")
                .token(token)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .fullName(user.getFullName())
                .role(user.getRole() != null ? user.getRole().getCode() : null)
                .build();
    }

    @Override
    @Transactional
    public LoginResponse refreshByRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new UnauthorizedException("refreshToken không hợp lệ");
        }

        String hash = hashToken(refreshToken.trim());
        AuthRefreshTokenEntity tokenEntity = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new UnauthorizedException("refreshToken không hợp lệ"));

        if (Boolean.TRUE.equals(tokenEntity.getRevoked()) || tokenEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("refreshToken đã hết hạn hoặc bị thu hồi");
        }

        UserEntity user = tokenEntity.getUser();
        if (user == null || user.getStatus() == 0) {
            throw new UnauthorizedException("Tài khoản không hợp lệ");
        }

        tokenEntity.setRevoked(true);
        tokenEntity.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(tokenEntity);

        String accessToken = generateToken(user);
        String newRefreshToken = issueRefreshToken(user);

        return LoginResponse.builder()
                .token(accessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .fullName(user.getFullName())
                .role(user.getRole() != null ? user.getRole().getCode() : null)
                .build();
    }
    @Override
    @Transactional
    public void logoutByRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }

        String hash = hashToken(refreshToken.trim());
        refreshTokenRepository.findByTokenHash(hash).ifPresent(tokenEntity -> {
            tokenEntity.setRevoked(true);
            tokenEntity.setRevokedAt(LocalDateTime.now());
            refreshTokenRepository.save(tokenEntity);
        });
    }

    @Override
    @Transactional
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest req) {
        String identifier = req.getIdentifier() != null ? req.getIdentifier().trim() : "";
        if (identifier.isBlank()) {
            throw new BusinessException("identifier không được để trống");
        }

        UserEntity user = null;
        String normalizedPhone = PhoneUtil.normalize(identifier);
        if (normalizedPhone != null) {
            user = userRepo.findByPhone(normalizedPhone).orElse(null);
        }

        if (user == null) {
            user = userRepo.findByEmail(identifier.toLowerCase()).orElse(null);
        }

        // Trả thông điệp chung để tránh user enumeration.
        if (user == null) {
            return ForgotPasswordResponse.builder()
                    .message("Nếu tài khoản tồn tại, mã xác nhận đã được gửi về email")
                    .build();
        }

        String normalizedEmail = normalizeEmail(user.getEmail());
        if (normalizedEmail == null) {
            throw new BusinessException("Tài khoản chưa liên kết email để nhận mã xác nhận");
        }

        LocalDateTime now = LocalDateTime.now();
        revokeOutstandingResetTokens(user.getId(), now);

        String resetCode = generatePasswordResetCode(normalizedEmail);
        String tokenHash = hashPasswordResetCode(normalizedEmail, resetCode);

        PasswordResetTokenEntity entity = PasswordResetTokenEntity.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(now.plusMinutes(passwordResetSettings.getExpiryMinutes()))
                .used(false)
                .createdAt(now)
                .build();

        passwordResetTokenRepository.save(entity);
        passwordResetEmailService.sendResetCode(normalizedEmail, user.getFullName(), resetCode, passwordResetSettings.getExpiryMinutes());

        return ForgotPasswordResponse.builder()
                .message("Nếu tài khoản tồn tại, mã xác nhận đã được gửi về email")
                .maskedEmail(maskEmail(normalizedEmail))
                .expiresInMinutes(passwordResetSettings.getExpiryMinutes())
                .build();
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        String normalizedEmail = normalizeEmail(req.getEmail());
        String rawCode = req.getCode() != null ? req.getCode().trim() : "";

        String tokenHash = hashPasswordResetCode(normalizedEmail, rawCode);
        PasswordResetTokenEntity tokenEntity = passwordResetTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessException("Mã xác nhận không hợp lệ hoặc đã hết hạn"));

        if (Boolean.TRUE.equals(tokenEntity.getUsed())) {
            throw new BusinessException("Mã xác nhận đã được sử dụng");
        }
        if (tokenEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Mã xác nhận đã hết hạn");
        }

        UserEntity user = tokenEntity.getUser();
        if (!normalizedEmail.equals(normalizeEmail(user.getEmail()))) {
            throw new BusinessException("Mã xác nhận không hợp lệ hoặc đã hết hạn");
        }

        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepo.save(user);

        tokenEntity.setUsed(true);
        tokenEntity.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(tokenEntity);

        List<AuthRefreshTokenEntity> activeTokens = refreshTokenRepository.findByUserIdAndRevokedFalse(user.getId());
        LocalDateTime now = LocalDateTime.now();
        activeTokens.forEach(t -> {
            t.setRevoked(true);
            t.setRevokedAt(now);
        });
        refreshTokenRepository.saveAll(activeTokens);
    }

    @Override
    public UserProfileResponse getUserProfile(Long userId) {
        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        String role = user.getRole() != null ? user.getRole().getCode() : null;
        return UserProfileResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName() == null ? "" : user.getFullName())
                .phone(user.getPhone() == null ? "" : user.getPhone())
                .email(user.getEmail() == null ? "" : user.getEmail())
                .role(role == null ? "" : role)
                .rescueRequestBlocked(Boolean.TRUE.equals(user.getRescueRequestBlocked()))
                .rescueRequestBlockedReason(user.getRescueRequestBlockedReason() == null ? "" : user.getRescueRequestBlockedReason())
                .build();
    }

    private String issueRefreshToken(UserEntity user) {
        String rawToken = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");

        AuthRefreshTokenEntity token = AuthRefreshTokenEntity.builder()
                .user(user)
                .tokenHash(hashToken(rawToken))
                .expiresAt(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .createdAt(LocalDateTime.now())
                .build();

        refreshTokenRepository.save(token);
        return rawToken;
    }

    private void revokeOutstandingResetTokens(Long userId, LocalDateTime now) {
        List<PasswordResetTokenEntity> activeTokens = passwordResetTokenRepository.findAllByUserIdAndUsedFalse(userId);
        if (activeTokens.isEmpty()) {
            return;
        }
        activeTokens.forEach(token -> {
            token.setUsed(true);
            token.setUsedAt(now);
        });
        passwordResetTokenRepository.saveAll(activeTokens);
    }

    private String generatePasswordResetCode(String normalizedEmail) {
        for (int attempt = 0; attempt < 10; attempt++) {
            String code = String.format("%0" + PASSWORD_RESET_CODE_LENGTH + "d",
                    ThreadLocalRandom.current().nextInt((int) Math.pow(10, PASSWORD_RESET_CODE_LENGTH)));
            String tokenHash = hashPasswordResetCode(normalizedEmail, code);
            if (passwordResetTokenRepository.findByTokenHash(tokenHash).isEmpty()) {
                return code;
            }
        }
        throw new IllegalStateException("Không thể tạo mã xác nhận duy nhất");
    }

    private String hashPasswordResetCode(String normalizedEmail, String code) {
        return hashToken(normalizedEmail + ":" + code);
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase();
    }

    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return "***";
        }
        String localPart = email.substring(0, atIndex);
        String domain = email.substring(atIndex + 1);
        String maskedLocal = localPart.charAt(0) + "***" + localPart.charAt(localPart.length() - 1);
        int dotIndex = domain.indexOf('.');
        if (dotIndex <= 1) {
            return maskedLocal + "@***";
        }
        String domainName = domain.substring(0, dotIndex);
        String suffix = domain.substring(dotIndex);
        return maskedLocal + "@" + domainName.charAt(0) + "***" + suffix;
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Không thể khởi tạo SHA-256", e);
        }
    }
}