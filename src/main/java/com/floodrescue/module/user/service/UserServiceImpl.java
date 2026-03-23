package com.floodrescue.module.user.service;

import com.floodrescue.module.user.dto.request.ChangeMyPasswordRequest;
import com.floodrescue.module.user.dto.request.UpdateMyProfileRequest;
import com.floodrescue.module.user.dto.response.UserProfileResponse;
import com.floodrescue.module.user.entity.UserEntity;
import com.floodrescue.module.user.repository.UserRepository;
import com.floodrescue.shared.exception.BusinessException;
import com.floodrescue.shared.exception.NotFoundException;
import com.floodrescue.shared.util.PhoneUtil;
import com.floodrescue.shared.util.TextNormalizationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(Long userId) {
        return toProfileResponse(findUserById(userId));
    }

    @Override
    @Transactional
    public UserProfileResponse updateMyProfile(Long userId, UpdateMyProfileRequest request) {
        UserEntity user = findUserById(userId);

        String normalizedFullName = TextNormalizationUtil.cleanDisplayText(request.getFullName()).trim();
        String normalizedEmail = normalizeOptionalEmail(request.getEmail());
        String normalizedPhone = normalizeOptionalPhone(request.getPhone());

        if (normalizedEmail != null && userRepository.existsByEmailAndIdNot(normalizedEmail, userId)) {
            throw new BusinessException("Email đã tồn tại");
        }
        if (normalizedPhone != null && userRepository.existsByPhoneAndIdNot(normalizedPhone, userId)) {
            throw new BusinessException("Số điện thoại đã tồn tại");
        }

        user.setFullName(normalizedFullName);
        user.setEmail(normalizedEmail);
        user.setPhone(normalizedPhone);
        user.setUpdatedAt(LocalDateTime.now());

        return toProfileResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void changeMyPassword(Long userId, ChangeMyPasswordRequest request) {
        UserEntity user = findUserById(userId);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BusinessException("Mật khẩu hiện tại không đúng");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    private UserEntity findUserById(Long userId) {
        return userRepository.findByIdWithRole(userId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));
    }

    private String normalizeOptionalEmail(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim().toLowerCase();
    }

    private String normalizeOptionalPhone(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String normalized = PhoneUtil.normalize(value);
        if (normalized == null) {
            throw new BusinessException("Số điện thoại không hợp lệ");
        }
        return normalized;
    }

    private UserProfileResponse toProfileResponse(UserEntity user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .fullName(TextNormalizationUtil.cleanDisplayText(user.getFullName()))
                .phone(user.getPhone())
                .email(user.getEmail())
                .role(user.getRole() != null ? user.getRole().getCode() : null)
                .roleName(user.getRole() != null ? TextNormalizationUtil.cleanDisplayText(user.getRole().getName()) : null)
                .status(user.getStatus() != null && user.getStatus() == 1 ? "ACTIVE" : "LOCKED")
                .teamId(user.getTeamId())
                .isLeader(Boolean.TRUE.equals(user.getIsLeader()))
                .rescueRequestBlocked(Boolean.TRUE.equals(user.getRescueRequestBlocked()))
                .rescueRequestBlockedReason(TextNormalizationUtil.cleanDisplayText(user.getRescueRequestBlockedReason()))
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
