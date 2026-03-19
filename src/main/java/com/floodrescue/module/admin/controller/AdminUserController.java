package com.floodrescue.module.admin.controller;

import com.floodrescue.module.admin.dto.request.AdminCreateUserRequest;
import com.floodrescue.module.admin.dto.request.AdminResetPasswordRequest;
import com.floodrescue.module.admin.dto.request.AdminUpdateUserRequest;
import com.floodrescue.module.admin.dto.request.AdminUpdateUserStatusRequest;
import com.floodrescue.module.admin.service.AdminUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Validated
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer roleId
    ) {
        return ResponseEntity.ok(adminUserService.getUsers(page, size, keyword, roleId));
    }

    @PostMapping("/create-user")
    public ResponseEntity<Map<String, Object>> createUser(
            @Valid @RequestBody AdminCreateUserRequest payload,
            Authentication authentication,
            HttpServletRequest request
    ) {
        return adminUserService.createUser(payload, getCurrentUserId(authentication), request);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateUserRequest payload,
            Authentication authentication,
            HttpServletRequest request
    ) {
        return adminUserService.updateUser(id, payload, getCurrentUserId(authentication), request);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(
            @PathVariable Long id,
            Authentication authentication,
            HttpServletRequest request
    ) {
        return adminUserService.deleteUser(id, getCurrentUserId(authentication), request);
    }

    @PutMapping("/users/{id}/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody AdminResetPasswordRequest payload,
            Authentication authentication,
            HttpServletRequest request
    ) {
        return adminUserService.resetPassword(id, payload, getCurrentUserId(authentication), request);
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateUserStatusRequest payload,
            Authentication authentication,
            HttpServletRequest request
    ) {
        return adminUserService.updateStatus(id, payload, getCurrentUserId(authentication), request);
    }

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return Long.parseLong(userDetails.getUsername());
    }
}
