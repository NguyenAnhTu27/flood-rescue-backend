package com.floodrescue.module.auth.controller;

import com.floodrescue.config.security.JwtTokenProvider;
import com.floodrescue.module.auth.dto.request.ForgotPasswordRequest;
import com.floodrescue.module.auth.dto.request.LoginRequest;
import com.floodrescue.module.auth.dto.request.RegisterCitizenRequest;
import com.floodrescue.module.auth.dto.request.ResetPasswordRequest;
import com.floodrescue.module.auth.dto.response.LoginResponse;
import com.floodrescue.module.auth.service.AuthService;
import com.floodrescue.module.user.entity.UserEntity;
import com.floodrescue.module.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return Long.parseLong(userDetails.getUsername());
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerCitizen(@Valid @RequestBody RegisterCitizenRequest req) {
        authService.registerCitizen(req);
        return ResponseEntity.ok(Map.of("message", "Đăng ký Citizen thành công"));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        UserEntity user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("message", "User not found"));
        }

        String role = user.getRole() != null ? user.getRole().getCode() : null;
        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "fullName", user.getFullName() == null ? "" : user.getFullName(),
                "phone", user.getPhone() == null ? "" : user.getPhone(),
                "email", user.getEmail() == null ? "" : user.getEmail(),
                "role", role == null ? "" : role,
                "rescueRequestBlocked", Boolean.TRUE.equals(user.getRescueRequestBlocked()),
                "rescueRequestBlockedReason", user.getRescueRequestBlockedReason() == null ? "" : user.getRescueRequestBlockedReason()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        return ResponseEntity.ok(Map.of("message", "Đăng xuất thành công"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        UserEntity user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getRole() == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getRole().getCode());
        return ResponseEntity.ok(Map.of(
                "token", token,
                "tokenType", "Bearer"
        ));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        // Do not reveal user existence; API always returns success message.
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        });

        return ResponseEntity.ok(Map.of(
                "message", "Nếu email tồn tại trong hệ thống, hướng dẫn đặt lại mật khẩu đã được gửi"
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        UserEntity user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.ok(Map.of(
                    "message", "Đặt lại mật khẩu thành công"
            ));
        }

        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFailedLoginAttempts(0);
        user.setLockedAt(null);
        user.setTempLockedUntil(null);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Đặt lại mật khẩu thành công"));
    }
}
