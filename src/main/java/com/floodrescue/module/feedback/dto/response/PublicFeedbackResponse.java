package com.floodrescue.module.feedback.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PublicFeedbackResponse {
    private Long id;
    private String citizenName;
    private Integer rating;
    private String feedbackContent;
    private Boolean rescuedConfirmed;
    private Boolean reliefConfirmed;
    private LocalDateTime createdAt;
}