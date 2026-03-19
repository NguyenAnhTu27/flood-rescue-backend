package com.floodrescue.module.feedback.controller;

import com.floodrescue.module.feedback.dto.response.PublicFeedbackResponse;
import com.floodrescue.module.feedback.dto.response.SystemFeedbackReplyResponse;
import com.floodrescue.module.feedback.dto.response.SystemFeedbackResponse;
import com.floodrescue.module.feedback.service.SystemFeedbackService;
import com.floodrescue.shared.dto.ApiResult;
import com.floodrescue.shared.dto.PagedData;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/public/feedbacks", "/api/public/citizen-feedbacks"})
public class PublicFeedbackController {

    private final SystemFeedbackService systemFeedbackService;

    @GetMapping({"", "/list"})
        public ResponseEntity<ApiResult<PagedData<PublicFeedbackResponse>>> getFeedbacks(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(normalizedPage, normalizedSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<PublicFeedbackResponse> response = systemFeedbackService.getFeedbacks(pageable)
                .map(this::toPublicResponse);
        return ResponseEntity.ok(ApiResult.ok(PagedData.from(response)));
    }

    @GetMapping({"/{feedbackId}/replies", "/list/{feedbackId}/replies"})
    public ResponseEntity<ApiResult<List<SystemFeedbackReplyResponse>>> getReplies(@PathVariable Long feedbackId) {
        return ResponseEntity.ok(ApiResult.ok(systemFeedbackService.getReplies(feedbackId)));
    }

    private PublicFeedbackResponse toPublicResponse(SystemFeedbackResponse feedback) {
        return PublicFeedbackResponse.builder()
                .id(feedback.getId())
                .citizenName(feedback.getCitizenName())
                .rating(feedback.getRating())
                .feedbackContent(feedback.getFeedbackContent())
                .rescuedConfirmed(feedback.getRescuedConfirmed())
                .reliefConfirmed(feedback.getReliefConfirmed())
                .createdAt(feedback.getCreatedAt())
                .build();
    }
}