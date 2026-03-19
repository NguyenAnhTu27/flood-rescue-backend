package com.floodrescue.module.feedback.repository;

import com.floodrescue.module.feedback.entity.SystemFeedbackReplyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SystemFeedbackReplyRepository extends JpaRepository<SystemFeedbackReplyEntity, Long> {

    List<SystemFeedbackReplyEntity> findAllByFeedbackIdOrderByCreatedAtAsc(Long feedbackId);
}