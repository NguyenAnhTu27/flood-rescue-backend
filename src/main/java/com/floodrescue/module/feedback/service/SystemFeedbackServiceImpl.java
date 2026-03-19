package com.floodrescue.module.feedback.service;

import com.floodrescue.module.feedback.dto.request.SystemFeedbackCreateRequest;
import com.floodrescue.module.feedback.dto.request.SystemFeedbackReplyCreateRequest;
import com.floodrescue.module.feedback.dto.response.SystemFeedbackReplyResponse;
import com.floodrescue.module.feedback.dto.response.SystemFeedbackResponse;
import com.floodrescue.module.feedback.dto.response.SystemFeedbackSummaryResponse;
import com.floodrescue.module.feedback.entity.SystemFeedbackEntity;
import com.floodrescue.module.feedback.entity.SystemFeedbackReplyEntity;
import com.floodrescue.module.feedback.repository.SystemFeedbackReplyRepository;
import com.floodrescue.module.feedback.repository.SystemFeedbackRepository;
import com.floodrescue.module.user.entity.UserEntity;
import com.floodrescue.module.user.repository.UserRepository;
import com.floodrescue.shared.exception.BusinessException;
import com.floodrescue.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SystemFeedbackServiceImpl implements SystemFeedbackService {

    private final SystemFeedbackRepository feedbackRepository;
    private final SystemFeedbackReplyRepository feedbackReplyRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public SystemFeedbackResponse createFeedback(Long citizenId, SystemFeedbackCreateRequest request) {
        UserEntity citizen = userRepository.findById(citizenId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy công dân."));

        String content = normalizeText(request.getFeedbackContent());

        SystemFeedbackEntity saved = feedbackRepository.save(SystemFeedbackEntity.builder()
                .citizen(citizen)
                .rating(request.getRating())
                .feedbackContent(content)
                .rescuedConfirmed(Boolean.TRUE.equals(request.getRescuedConfirmed()))
                .reliefConfirmed(Boolean.TRUE.equals(request.getReliefConfirmed()))
            .deleted(false)
                .build());

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SystemFeedbackResponse> getFeedbacks(Pageable pageable) {
        return feedbackRepository.findAllByDeletedFalse(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SystemFeedbackResponse> getFeedbacksByCitizenId(Long citizenId, Pageable pageable) {
        return feedbackRepository.findByCitizenIdAndDeletedFalse(citizenId, pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public SystemFeedbackReplyResponse createReply(Long feedbackId, Long userId, SystemFeedbackReplyCreateRequest request) {
        SystemFeedbackEntity feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phản hồi."));
        if (Boolean.TRUE.equals(feedback.getDeleted())) {
            throw new NotFoundException("Không tìm thấy phản hồi.");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng."));

        String roleCode = user.getRole() == null ? null : user.getRole().getCode();
        if (roleCode == null || (!"CITIZEN".equals(roleCode) && !"ADMIN".equals(roleCode))) {
            throw new BusinessException("Chỉ công dân hoặc admin mới được phản hồi");
        }

        String normalizedContent = normalizeText(request.getContent());
        if (normalizedContent == null) {
            throw new BusinessException("Nội dung phản hồi không được để trống");
        }

        SystemFeedbackReplyEntity saved = feedbackReplyRepository.save(SystemFeedbackReplyEntity.builder()
                .feedback(feedback)
                .user(user)
                .roleCode(roleCode)
                .content(normalizedContent)
                .build());

        return toReplyResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SystemFeedbackReplyResponse> getReplies(Long feedbackId) {
        if (!feedbackRepository.existsByIdAndDeletedFalse(feedbackId)) {
            throw new NotFoundException("Không tìm thấy phản hồi.");
        }
        return feedbackReplyRepository.findAllByFeedbackIdOrderByCreatedAtAsc(feedbackId)
                .stream()
                .map(this::toReplyResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteFeedback(Long feedbackId, Long adminId, String reason) {
        SystemFeedbackEntity feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phản hồi."));
        if (Boolean.TRUE.equals(feedback.getDeleted())) {
            return;
        }
        feedback.setDeleted(true);
        feedback.setDeletedAt(java.time.LocalDateTime.now());
        feedback.setDeletedByUserId(adminId);
        feedback.setDeleteReason(normalizeReason(reason));
        feedbackRepository.save(feedback);
    }

    @Override
    @Transactional(readOnly = true)
    public SystemFeedbackSummaryResponse getSummary() {
        long total = feedbackRepository.countByDeletedFalse();
        Double avg = feedbackRepository.findAverageRating();

        Map<Integer, Long> distribution = new LinkedHashMap<>();
        for (int star = 5; star >= 1; star--) {
            distribution.put(star, feedbackRepository.countByDeletedFalseAndRating(star));
        }

        return SystemFeedbackSummaryResponse.builder()
                .totalFeedbacks(total)
                .averageRating(avg == null ? 0.0 : Math.round(avg * 100.0) / 100.0)
                .ratingDistribution(distribution)
                .build();
    }

    private SystemFeedbackResponse toResponse(SystemFeedbackEntity entity) {
        UserEntity citizen = entity.getCitizen();
        return SystemFeedbackResponse.builder()
                .id(entity.getId())
                .citizenId(citizen == null ? null : citizen.getId())
                .citizenName(citizen == null ? null : citizen.getFullName())
                .citizenEmail(citizen == null ? null : citizen.getEmail())
                .rating(entity.getRating())
                .feedbackContent(entity.getFeedbackContent())
                .rescuedConfirmed(Boolean.TRUE.equals(entity.getRescuedConfirmed()))
                .reliefConfirmed(Boolean.TRUE.equals(entity.getReliefConfirmed()))
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private String normalizeText(String input) {
        if (input == null) return null;
        String normalized = input.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeReason(String reason) {
        if (reason == null) {
            return null;
        }
        String normalized = reason.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        return normalized.length() > 500 ? normalized.substring(0, 500) : normalized;
    }

    private SystemFeedbackReplyResponse toReplyResponse(SystemFeedbackReplyEntity entity) {
        UserEntity user = entity.getUser();
        return SystemFeedbackReplyResponse.builder()
                .id(entity.getId())
                .feedbackId(entity.getFeedback() == null ? null : entity.getFeedback().getId())
                .userId(user == null ? null : user.getId())
                .userName(user == null ? null : user.getFullName())
                .userRole(entity.getRoleCode())
                .content(entity.getContent())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
