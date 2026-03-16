package com.floodrescue.module.admin.controller;

import com.floodrescue.module.admin.service.AuditLogService;
import com.floodrescue.module.admin.service.NotificationTemplateService;
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
public class NotificationTemplateController extends AbstractAdminController {

    private final NotificationTemplateService notificationTemplateService;
    private final AuditLogService auditLogService;

    @GetMapping("/notification-templates")
    public ResponseEntity<Map<String, Object>> getNotificationTemplates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword
    ) {
        return ResponseEntity.ok(notificationTemplateService.getNotificationTemplates(page, size, keyword));
    }

    @PostMapping("/notification-templates")
    public ResponseEntity<Map<String, Object>> createNotificationTemplate(
            @RequestBody Map<String, Object> payload,
            Authentication authentication,
            HttpServletRequest request
    ) {
        String code = notificationTemplateService.createNotificationTemplate(payload);
        Long actorId = getCurrentUserId(authentication);
        auditLogService.writeAudit(actorId, "CREATE_NOTIFICATION_TEMPLATE", "NOTIFICATION_TEMPLATE",
                null, "SUCCESS", "Tạo mẫu thông báo", code, request, null, payload);
        return ResponseEntity.ok(Map.of("message", "Tạo mẫu thông báo thành công"));
    }

    @PutMapping("/notification-templates/{id}")
    public ResponseEntity<Map<String, Object>> updateNotificationTemplate(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> payload,
            Authentication authentication,
            HttpServletRequest request
    ) {
        notificationTemplateService.updateNotificationTemplate(id, payload);
        Long actorId = getCurrentUserId(authentication);
        auditLogService.writeAudit(actorId, "UPDATE_NOTIFICATION_TEMPLATE", "NOTIFICATION_TEMPLATE",
                id.longValue(), "SUCCESS", "Cập nhật mẫu thông báo", String.valueOf(id), request, null, payload);
        return ResponseEntity.ok(Map.of("message", "Cập nhật mẫu thông báo thành công"));
    }

    @PatchMapping("/notification-templates/{id}/active")
    public ResponseEntity<Map<String, Object>> toggleNotificationTemplateActive(@PathVariable Integer id) {
        notificationTemplateService.toggleNotificationTemplateActive(id);
        return ResponseEntity.ok(Map.of("message", "Đã cập nhật trạng thái"));
    }

    @DeleteMapping("/notification-templates/{id}")
    public ResponseEntity<Map<String, Object>> deleteNotificationTemplate(@PathVariable Integer id) {
        notificationTemplateService.deleteNotificationTemplate(id);
        return ResponseEntity.ok(Map.of("message", "Xóa mẫu thông báo thành công"));
    }
}
