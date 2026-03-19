package com.floodrescue.module.admin.controller;

import com.floodrescue.module.admin.dto.request.AdminContentPagesUpdateRequest;
import com.floodrescue.module.admin.service.AuditLogService;
import com.floodrescue.module.admin.service.SystemSettingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class SystemSettingController {

    private final SystemSettingService systemSettingService;
    private final AuditLogService auditLogService;

    @GetMapping("/system-settings")
    public ResponseEntity<Map<String, Object>> getSystemSettings() {
        return ResponseEntity.ok(systemSettingService.getSystemSettingsResponse());
    }

    @PutMapping("/system-settings")
    public ResponseEntity<Map<String, Object>> updateSystemSettings(
            @Valid @RequestBody Map<
                    @NotBlank(message = "Key cấu hình không được để trống")
                    @Size(max = 100, message = "Key cấu hình không được vượt quá 100 ký tự")
                    String,
                    @NotNull(message = "Giá trị cấu hình không được để trống")
                    @Size(max = 10000, message = "Giá trị cấu hình không được vượt quá 10000 ký tự")
                    String> payload,
            Authentication authentication,
            HttpServletRequest request
    ) {
        Long actorId = getCurrentUserId(authentication);
        systemSettingService.updateSystemSettings(payload, actorId);
        auditLogService.writeAudit(actorId, "UPDATE_SYSTEM_SETTINGS", "SYSTEM", null, "SUCCESS", "Cập nhật cấu hình hệ thống", null, request, null, payload);
        return ResponseEntity.ok(Map.of("message", "Đã lưu cấu hình"));
    }

    @GetMapping("/content-pages")
    public ResponseEntity<Map<String, Object>> getContentPages() {
        return ResponseEntity.ok(systemSettingService.getContentPages());
    }

    @PutMapping("/content-pages")
    public ResponseEntity<Map<String, Object>> updateContentPages(
            @Valid @RequestBody AdminContentPagesUpdateRequest payload,
            Authentication authentication,
            HttpServletRequest request
    ) {
        Long actorId = getCurrentUserId(authentication);
        systemSettingService.updateContentPages(payload, actorId);
        auditLogService.writeAudit(actorId, "UPDATE_CONTENT_PAGES", "SYSTEM", null, "SUCCESS", "Cập nhật nội dung trang công khai", null, request, null, payload);
        return ResponseEntity.ok(Map.of("message", "Đã lưu nội dung trang"));
    }

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return Long.parseLong(userDetails.getUsername());
    }
}
