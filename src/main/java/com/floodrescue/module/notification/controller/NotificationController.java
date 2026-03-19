package com.floodrescue.module.notification.controller;

import com.floodrescue.module.notification.dto.NotificationResponse;
import com.floodrescue.module.notification.dto.OverloadEmergencyRequest;
import com.floodrescue.module.notification.dto.QueueEmergencyRequest;
import com.floodrescue.module.notification.service.NotificationService;
import com.floodrescue.shared.dto.ApiResult;
import com.floodrescue.shared.dto.PagedData;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return Long.parseLong(userDetails.getUsername());
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResult<PagedData<NotificationResponse>>> getMyNotifications(
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication
    ) {
        Long userId = getCurrentUserId(authentication);
        Page<NotificationResponse> response = notificationService.getMyNotifications(userId, unreadOnly, pageable);
        return ResponseEntity.ok(ApiResult.ok(PagedData.from(response)));
    }

    @GetMapping("/me/unread-count")
    public ResponseEntity<ApiResult<Map<String, Long>>> getUnreadCount(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(ApiResult.ok(Map.of("unreadCount", notificationService.countUnread(userId))));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<ApiResult<Void>> markRead(@PathVariable Long id, Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        notificationService.markRead(userId, id);
        return ResponseEntity.ok(ApiResult.ok("Đã xác nhận đã xem"));
    }

    @PostMapping("/me/read-all")
    public ResponseEntity<ApiResult<Void>> markAllRead(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        notificationService.markAllRead(userId);
        return ResponseEntity.ok(ApiResult.ok("Đã đánh dấu tất cả là đã xem"));
    }

    @PostMapping("/{id}/queue")
    public ResponseEntity<ApiResult<Map<String, Object>>> queueEmergency(
            @PathVariable Long id,
            @RequestBody(required = false) QueueEmergencyRequest request,
            Authentication authentication
    ) {
        Long userId = getCurrentUserId(authentication);
        boolean direct = request == null || request.getDirect() == null || request.getDirect();
        String note = request != null ? request.getNote() : null;
        Long queuedRequestId = notificationService.queueEmergencyRequest(userId, id, direct, note);
        return ResponseEntity.ok(ApiResult.ok(Map.of(
                "message", "Đã đưa yêu cầu khẩn cấp vào hàng đợi",
                "queuedRequestId", queuedRequestId
        )));
    }

    @PostMapping("/emergency/overload")
        public ResponseEntity<ApiResult<Map<String, Object>>> overloadEmergency(
            @RequestBody OverloadEmergencyRequest request,
            Authentication authentication
    ) {
        Long userId = getCurrentUserId(authentication);
        Long queueRequestId = request != null ? request.getQueueRequestId() : null;
        String note = request != null ? request.getNote() : null;
        notificationService.markEmergencyOverloaded(userId, queueRequestId, note);
        return ResponseEntity.ok(ApiResult.ok(Map.of(
                "message", "Đã báo quá tải và giữ yêu cầu trong hàng đợi",
                "queueRequestId", queueRequestId
        )));
    }
}
