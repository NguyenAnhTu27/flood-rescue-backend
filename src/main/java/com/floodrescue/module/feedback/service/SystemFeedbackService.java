package com.floodrescue.module.feedback.service;

import com.floodrescue.module.feedback.dto.request.SystemFeedbackCreateRequest;
import com.floodrescue.module.feedback.dto.request.SystemFeedbackReplyCreateRequest;
import com.floodrescue.module.feedback.dto.response.SystemFeedbackReplyResponse;
import com.floodrescue.module.feedback.dto.response.SystemFeedbackResponse;
import com.floodrescue.module.feedback.dto.response.SystemFeedbackSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SystemFeedbackService {
    SystemFeedbackResponse createFeedback(Long citizenId, SystemFeedbackCreateRequest request);

    Page<SystemFeedbackResponse> getFeedbacks(Pageable pageable);

    Page<SystemFeedbackResponse> getFeedbacksByCitizenId(Long citizenId, Pageable pageable);

    SystemFeedbackReplyResponse createReply(Long feedbackId, Long userId, SystemFeedbackReplyCreateRequest request);

    List<SystemFeedbackReplyResponse> getReplies(Long feedbackId);

    void deleteFeedback(Long feedbackId, Long adminId, String reason);

    SystemFeedbackSummaryResponse getSummary();
}
