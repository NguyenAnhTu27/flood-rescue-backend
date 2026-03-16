package com.floodrescue.module.auth.controller;

import com.floodrescue.module.auth.dto.request.ForgotPasswordRequest;
import com.floodrescue.module.auth.dto.request.LoginRequest;
import com.floodrescue.module.auth.dto.request.LogoutRequest;
import com.floodrescue.module.auth.dto.request.RefreshTokenRequest;
import com.floodrescue.module.auth.dto.request.RegisterCitizenRequest;
import com.floodrescue.module.auth.dto.request.ResetPasswordRequest;
import com.floodrescue.module.auth.dto.response.ForgotPasswordResponse;
import com.floodrescue.module.auth.dto.response.LoginResponse;
import com.floodrescue.module.auth.dto.response.UserProfileResponse;
import com.floodrescue.module.auth.service.AuthService;
import com.floodrescue.module.auth.service.LoginAttemptLimiter;
import com.floodrescue.shared.dto.ApiResult;
import com.floodrescue.shared.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final LoginAttemptLimiter loginAttemptLimiter;

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return Long.parseLong(userDetails.getUsername());
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResult<LoginResponse>> registerCitizen(@Valid @RequestBody RegisterCitizenRequest req) {
        LoginResponse response = authService.registerAndLogin(req);
        return ResponseEntity.ok(ApiResult.ok(response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResult<LoginResponse>> login(
            @Valid @RequestBody LoginRequest req,
            HttpServletRequest request
    ) {
        String key = buildLoginAttemptKey(request, req.getIdentifier());

        if (loginAttemptLimiter.isBlocked(key)) {
            long retryAfter = loginAttemptLimiter.getRetryAfterSeconds(key);
            throw new UnauthorizedException("Đăng nhập thất bại quá nhiều lần. Vui lòng thử lại sau " + retryAfter + " giây");
        }

        try {
            LoginResponse response = authService.login(req);
            loginAttemptLimiter.recordSuccess(key);
            return ResponseEntity.ok(ApiResult.ok(response));
        } catch (UnauthorizedException ex) {
            loginAttemptLimiter.recordFailure(key);
            throw ex;
        }
    }

    private String buildLoginAttemptKey(HttpServletRequest request, String identifier) {
        String ip = extractClientIp(request);
        String normalizedIdentifier = identifier == null ? "" : identifier.trim().toLowerCase();
        return ip + "|" + normalizedIdentifier;
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResult<UserProfileResponse>> me(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        if (userId == null) {
            throw new UnauthorizedException("Unauthorized");
        }
        UserProfileResponse response = authService.getUserProfile(userId);
        return ResponseEntity.ok(ApiResult.ok(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResult<Void>> logout(@RequestBody(required = false) LogoutRequest request) {
        if (request != null && request.getRefreshToken() != null) {
            authService.logoutByRefreshToken(request.getRefreshToken());
        }
        return ResponseEntity.ok(ApiResult.ok("Đăng xuất thành công"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResult<LoginResponse>> refreshToken(
            @RequestBody RefreshTokenRequest request
    ) {
        if (request == null || request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
            throw new UnauthorizedException("refreshToken không được để trống");
        }
        return ResponseEntity.ok(ApiResult.ok(authService.refreshByRefreshToken(request.getRefreshToken())));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResult<ForgotPasswordResponse>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(ApiResult.ok(authService.forgotPassword(request)));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResult<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResult.ok("Đặt lại mật khẩu thành công"));
    }
}
