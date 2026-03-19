package com.floodrescue.module.feedback.entity;

import com.floodrescue.module.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "system_feedback_replies", indexes = {
        @Index(name = "idx_feedback_replies_feedback", columnList = "feedback_id"),
        @Index(name = "idx_feedback_replies_user", columnList = "user_id"),
        @Index(name = "idx_feedback_replies_created", columnList = "created_at")
})
public class SystemFeedbackReplyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feedback_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_feedback_replies_feedback"))
    private SystemFeedbackEntity feedback;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_feedback_replies_user"))
    private UserEntity user;

    @Column(nullable = false, length = 30)
    private String roleCode;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}