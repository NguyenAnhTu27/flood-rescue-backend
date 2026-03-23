package com.floodrescue.module.user.controller;

import com.floodrescue.module.user.dto.request.ChangeMyPasswordRequest;
import com.floodrescue.module.user.dto.request.UpdateMyProfileRequest;
import com.floodrescue.module.user.dto.response.UserProfileResponse;
import com.floodrescue.module.user.service.UserService;
import com.floodrescue.shared.exception.UnauthorizedException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(Authentication authentication) {
        return ResponseEntity.ok(userService.getMyProfile(getCurrentUserId(authentication)));
    }

    @PutMapping("/me/profile")
    public ResponseEntity<UserProfileResponse> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateMyProfileRequest request
    ) {
        return ResponseEntity.ok(userService.updateMyProfile(getCurrentUserId(authentication), request));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Map<String, Object>> changeMyPassword(
            Authentication authentication,
            @Valid @RequestBody ChangeMyPasswordRequest request
    ) {
        userService.changeMyPassword(getCurrentUserId(authentication), request);
        return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công"));
    }

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new UnauthorizedException("Bạn chưa đăng nhập");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return Long.parseLong(userDetails.getUsername());
    }
}
