package com.floodrescue.module.feedback.controller;

import com.floodrescue.module.admin.service.AuditLogService;
import com.floodrescue.module.feedback.dto.request.SystemFeedbackReplyCreateRequest;
import com.floodrescue.module.feedback.dto.response.SystemFeedbackReplyResponse;
import com.floodrescue.module.feedback.dto.response.SystemFeedbackResponse;
import com.floodrescue.module.feedback.dto.response.SystemFeedbackSummaryResponse;
import com.floodrescue.module.feedback.service.SystemFeedbackService;
import com.floodrescue.shared.dto.ApiResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;

@RestController
@RequestMapping("/api/feedback/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminFeedbackController {

    private final SystemFeedbackService systemFeedbackService;
    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<Page<SystemFeedbackResponse>> getFeedbacks(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(systemFeedbackService.getFeedbacks(pageable));
    }

    @GetMapping("/summary")
    public ResponseEntity<SystemFeedbackSummaryResponse> getSummary() {
        return ResponseEntity.ok(systemFeedbackService.getSummary());
    }

    @PostMapping("/{feedbackId}/replies")
    public ResponseEntity<SystemFeedbackReplyResponse> createReply(
            @PathVariable Long feedbackId,
            @Valid @RequestBody SystemFeedbackReplyCreateRequest request,
            Authentication authentication
    ) {
        Long adminId = getCurrentUserId(authentication);
        return ResponseEntity.ok(systemFeedbackService.createReply(feedbackId, adminId, request));
    }

    @GetMapping("/{feedbackId}/replies")
    public ResponseEntity<List<SystemFeedbackReplyResponse>> getReplies(@PathVariable Long feedbackId) {
        return ResponseEntity.ok(systemFeedbackService.getReplies(feedbackId));
    }

    @DeleteMapping("/{feedbackId}")
    public ResponseEntity<ApiResult<Void>> deleteFeedback(
            @PathVariable Long feedbackId,
            @RequestParam(name = "reason", required = false) String reason,
            Authentication authentication,
            HttpServletRequest request
    ) {
        Long adminId = getCurrentUserId(authentication);
        systemFeedbackService.deleteFeedback(feedbackId, adminId, reason);

        LinkedHashMap<String, Object> newData = new LinkedHashMap<>();
        newData.put("feedbackId", feedbackId);
        newData.put("reason", reason);
        auditLogService.writeAudit(
                adminId,
                "DELETE_FEEDBACK",
                "SYSTEM_FEEDBACK",
                feedbackId,
                "WARN",
                "Xóa mềm phản hồi của người dùng",
                null,
                request,
                null,
                newData
        );
        return ResponseEntity.ok(ApiResult.ok("Xóa phản hồi thành công"));
    }

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return Long.parseLong(userDetails.getUsername());
    }
}
