package com.floodrescue.module.admin.controller;

import com.floodrescue.module.admin.service.AuditLogService;
import com.floodrescue.module.admin.service.SystemSettingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SystemSettingController extends AbstractAdminController {

    private final SystemSettingService systemSettingService;
    private final AuditLogService auditLogService;

    @GetMapping("/system-settings")
    public ResponseEntity<Map<String, Object>> getSystemSettings() {
        return ResponseEntity.ok(Map.of("values", systemSettingService.getAllSystemSettings()));
    }

    @PutMapping("/system-settings")
    public ResponseEntity<Map<String, Object>> updateSystemSettings(
            @RequestBody Map<String, Object> payload,
            Authentication authentication,
            HttpServletRequest request
    ) {
        Long actorId = getCurrentUserId(authentication);
        systemSettingService.updateSystemSettings(payload, actorId);
        auditLogService.writeAudit(actorId, "UPDATE_SYSTEM_SETTINGS", "SYSTEM", null, "SUCCESS",
                "Cập nhật cấu hình hệ thống", null, request, null, payload);
        return ResponseEntity.ok(Map.of("message", "Đã lưu cấu hình"));
    }

    @GetMapping("/content-pages")
    public ResponseEntity<Map<String, Object>> getContentPages() {
        return ResponseEntity.ok(systemSettingService.getContentPages());
    }

    @PutMapping("/content-pages")
    public ResponseEntity<Map<String, Object>> updateContentPages(
            @RequestBody Map<String, Object> payload,
            Authentication authentication,
            HttpServletRequest request
    ) {
        Long actorId = getCurrentUserId(authentication);
        systemSettingService.updateContentPages(payload, actorId);
        auditLogService.writeAudit(actorId, "UPDATE_CONTENT_PAGES", "SYSTEM", null, "SUCCESS",
                "Cập nhật nội dung trang công khai", null, request, null, payload);
        return ResponseEntity.ok(Map.of("message", "Đã lưu nội dung trang"));
    }
}

