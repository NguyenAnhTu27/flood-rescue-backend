package com.floodrescue.module.admin.controller;

import com.floodrescue.module.admin.dto.request.AdminNotificationTemplateCreateRequest;
import com.floodrescue.module.admin.dto.request.AdminNotificationTemplateUpdateRequest;
import com.floodrescue.module.admin.service.AdminNotificationTemplateService;
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
public class AdminNotificationTemplateController {

    private final AdminNotificationTemplateService adminNotificationTemplateService;

    @GetMapping("/notification-templates")
    public ResponseEntity<Map<String, Object>> getNotificationTemplates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword
    ) {
        return ResponseEntity.ok(adminNotificationTemplateService.getNotificationTemplates(page, size, keyword));
    }

    @PostMapping("/notification-templates")
    public ResponseEntity<Map<String, Object>> createNotificationTemplate(
            @Valid @RequestBody AdminNotificationTemplateCreateRequest payload,
            Authentication authentication,
            HttpServletRequest request
    ) {
        return adminNotificationTemplateService.createNotificationTemplate(payload, getCurrentUserId(authentication), request);
    }

    @PutMapping("/notification-templates/{id}")
    public ResponseEntity<Map<String, Object>> updateNotificationTemplate(
            @PathVariable Integer id,
            @Valid @RequestBody AdminNotificationTemplateUpdateRequest payload,
            Authentication authentication,
            HttpServletRequest request
    ) {
        return adminNotificationTemplateService.updateNotificationTemplate(id, payload, getCurrentUserId(authentication), request);
    }

    @PatchMapping("/notification-templates/{id}/active")
    public ResponseEntity<Map<String, Object>> toggleNotificationTemplateActive(@PathVariable Integer id) {
        return adminNotificationTemplateService.toggleNotificationTemplateActive(id);
    }

    @DeleteMapping("/notification-templates/{id}")
    public ResponseEntity<Map<String, Object>> deleteNotificationTemplate(@PathVariable Integer id) {
        return adminNotificationTemplateService.deleteNotificationTemplate(id);
    }

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return Long.parseLong(userDetails.getUsername());
    }
}
