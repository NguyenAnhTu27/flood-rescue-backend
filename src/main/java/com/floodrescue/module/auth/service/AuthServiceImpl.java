package com.floodrescue.module.auth.service;

import com.floodrescue.config.security.JwtTokenProvider;
import com.floodrescue.module.admin.service.SystemSettingService;
import com.floodrescue.module.auth.dto.request.LoginRequest;
import com.floodrescue.module.auth.dto.request.RegisterCitizenRequest;
import com.floodrescue.module.auth.dto.response.LoginResponse;
import com.floodrescue.module.user.entity.RoleEntity;
import com.floodrescue.module.user.entity.UserEntity;
import com.floodrescue.module.user.repository.RoleRepository;
import com.floodrescue.module.user.repository.UserRepository;
import com.floodrescue.shared.exception.BusinessException;
import com.floodrescue.shared.exception.UnauthorizedException;
import com.floodrescue.shared.util.PhoneUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final SystemSettingService systemSettingService;

    @Override
    public void registerCitizen(RegisterCitizenRequest req) {
        if (systemSettingService.isMaintenanceModeEnabled()) {
            throw new BusinessException("Hệ thống đang bảo trì, tạm thời không thể đăng ký tài khoản");
        }

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
                .failedLoginAttempts(0)
                .lockedAt(null)
                .tempLockedUntil(null)
                .createdAt(now)
                .updatedAt(now)
                .build();

        userRepo.save(user);
    }

    @Override
    public LoginResponse login(LoginRequest req) {


        String identifier = req.getIdentifier().trim();

        // ===== DEFAULT ADMIN (KHÔNG PHỤ THUỘC DATABASE) =====
        if ("admin@gmail.com".equalsIgnoreCase(identifier)
                && "admin123".equals(req.getPassword())) {

            String token = jwtTokenProvider.generateToken(-1L, "ADMIN");

            return LoginResponse.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .userId(-1L)
                    .fullName("System Administrator")
                    .role("ADMIN")
                    .build();
        }

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

        if (systemSettingService.isMaintenanceModeEnabled() && !"ADMIN".equalsIgnoreCase(user.getRole().getCode())) {
            throw new UnauthorizedException("Hệ thống đang bảo trì, vui lòng thử lại sau");
        }

        if (user.getStatus() == 0) {
            throw new UnauthorizedException("Bạn đã bị khóa. Vui lòng liên hệ quản trị viên để mở khóa.");
        }

        LocalDateTime now = LocalDateTime.now();
        boolean justUnlockedFromTempLock = false;
        if (user.getTempLockedUntil() != null) {
            if (user.getTempLockedUntil().isAfter(now)) {
                throw new UnauthorizedException("Bạn đã bị khóa tạm thời. Vui lòng thử lại sau.");
            }
            // Lock hết hạn: cho phép thử lại, nhưng nếu sai sẽ khóa lại theo thời gian cấu hình.
            justUnlockedFromTempLock = true;
            user.setTempLockedUntil(null);
            user.setFailedLoginAttempts(0);
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            int threshold = systemSettingService.getAutoLockAfterFailedLogin();
            int lockMinutes = systemSettingService.getFailedLoginLockMinutes();

            if (justUnlockedFromTempLock) {
                user.setFailedLoginAttempts(1);
                user.setLockedAt(now);
                user.setTempLockedUntil(now.plusMinutes(lockMinutes));
                user.setUpdatedAt(now);
                userRepo.save(user);
                throw new UnauthorizedException("Bạn đã bị khóa tạm thời " + lockMinutes + " phút do nhập sai mật khẩu.");
            }

            int attempts = (user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts()) + 1;
            user.setFailedLoginAttempts(attempts);
            if (attempts >= threshold) {
                user.setLockedAt(now);
                user.setTempLockedUntil(now.plusMinutes(lockMinutes));
                user.setUpdatedAt(now);
                userRepo.save(user);
                throw new UnauthorizedException("Bạn đã bị khóa tạm thời " + lockMinutes + " phút do nhập sai mật khẩu quá " + threshold + " lần.");
            }

            user.setUpdatedAt(now);
            userRepo.save(user);
            throw new UnauthorizedException("Mật khẩu không đúng");
        }

        user.setLastLoginAt(now);
        user.setFailedLoginAttempts(0);
        user.setLockedAt(null);
        user.setTempLockedUntil(null);
        user.setUpdatedAt(now);
        userRepo.save(user);

        String roleCode = user.getRole().getCode();
        String token = jwtTokenProvider.generateToken(user.getId(), roleCode);

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .fullName(user.getFullName())
                .role(roleCode)
                .build();
    }
}
