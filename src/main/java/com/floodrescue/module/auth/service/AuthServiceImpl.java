package com.floodrescue.module.auth.service;

import com.floodrescue.config.security.JwtTokenProvider;
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
                    .message("Nếu tài khoản tồn tại, hệ thống đã tạo yêu cầu đặt lại mật khẩu")
                    .build();
        }

        String rawToken = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        String tokenHash = hashToken(rawToken);

        PasswordResetTokenEntity entity = PasswordResetTokenEntity.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .createdAt(LocalDateTime.now())
                .build();

        passwordResetTokenRepository.save(entity);

        // Dev fallback: trả token cho FE vì project chưa có hạ tầng email/SMS.
        return ForgotPasswordResponse.builder()
                .message("Mã đặt lại mật khẩu đã được tạo")
                .resetToken(rawToken)
                .expiresInMinutes(15)
                .build();
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        String rawToken = req.getToken() != null ? req.getToken().trim() : "";
        if (rawToken.isBlank()) {
            throw new BusinessException("token không được để trống");
        }

        String tokenHash = hashToken(rawToken);
        PasswordResetTokenEntity tokenEntity = passwordResetTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessException("Token đặt lại mật khẩu không hợp lệ"));

        if (Boolean.TRUE.equals(tokenEntity.getUsed())) {
            throw new BusinessException("Token đặt lại mật khẩu đã được sử dụng");
        }
        if (tokenEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Token đặt lại mật khẩu đã hết hạn");
        }

        UserEntity user = tokenEntity.getUser();
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