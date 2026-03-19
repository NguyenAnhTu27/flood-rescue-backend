package com.floodrescue.module.feedback.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SystemFeedbackReplyResponse {
    private Long id;
    private Long feedbackId;
    private Long userId;
    private String userName;
    private String userRole;
    private String content;
    private LocalDateTime createdAt;
}