package com.floodrescue.module.feedback.controller;

import com.floodrescue.module.feedback.dto.request.SystemFeedbackCreateRequest;
import com.floodrescue.module.feedback.dto.request.SystemFeedbackReplyCreateRequest;
import com.floodrescue.module.feedback.dto.response.SystemFeedbackReplyResponse;
import com.floodrescue.module.feedback.dto.response.SystemFeedbackResponse;
import com.floodrescue.module.feedback.service.SystemFeedbackService;
import com.floodrescue.shared.dto.ApiResult;
import com.floodrescue.shared.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/feedback/citizen")
@RequiredArgsConstructor
public class CitizenFeedbackController {

    private final SystemFeedbackService systemFeedbackService;

    @PostMapping
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<?> createFeedback(
            @Valid @RequestBody SystemFeedbackCreateRequest request,
            Authentication authentication
    ) {
        Long citizenId = getCurrentUserId(authentication);
        systemFeedbackService.createFeedback(citizenId, request);
        return ResponseEntity.ok(java.util.Map.of("message", "OK"));
    }

    @GetMapping
    public ResponseEntity<ApiResult<com.floodrescue.shared.dto.PagedData<SystemFeedbackResponse>>> getFeedbacks(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "createdAt") String sort
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort));
        Page<SystemFeedbackResponse> response = systemFeedbackService.getFeedbacks(pageable);
        return ResponseEntity.ok(ApiResult.ok(com.floodrescue.shared.dto.PagedData.from(response)));
    }

    /**
     * Get my feedbacks (citizen-only).
     * GET /api/feedback/my
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<PageResponse<SystemFeedbackResponse>> getMyFeedbacks(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "createdAt") String sort,
            Authentication authentication
    ) {
        Long citizenId = getCurrentUserId(authentication);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort));
        Page<SystemFeedbackResponse> response = systemFeedbackService.getFeedbacksByCitizenId(citizenId, pageable);
        return ResponseEntity.ok(PageResponse.from(response));
    }

    @PostMapping("/{feedbackId}/replies")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<ApiResult<SystemFeedbackReplyResponse>> createReply(
            @PathVariable Long feedbackId,
            @Valid @RequestBody SystemFeedbackReplyCreateRequest request,
            Authentication authentication
    ) {
        Long citizenId = getCurrentUserId(authentication);
        return ResponseEntity.ok(ApiResult.ok(systemFeedbackService.createReply(feedbackId, citizenId, request)));
    }

    @GetMapping("/{feedbackId}/replies")
    public ResponseEntity<ApiResult<List<SystemFeedbackReplyResponse>>> getReplies(@PathVariable Long feedbackId) {
        return ResponseEntity.ok(ApiResult.ok(systemFeedbackService.getReplies(feedbackId)));
    }

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            String username = userDetails.getUsername();
            try {
                return Long.parseLong(username);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        if (principal instanceof String s) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        if (principal instanceof Long l) {
            return l;
        }

        return null;
    }
}
