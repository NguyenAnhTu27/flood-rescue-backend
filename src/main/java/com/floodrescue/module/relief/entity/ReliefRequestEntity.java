package com.floodrescue.module.relief.entity;

import com.floodrescue.module.rescue.entity.RescueRequestEntity;
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
@Table(name = "relief_requests",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_relief_code", columnNames = "code")
        },
        indexes = {
                @Index(name = "idx_relief_status", columnList = "status"),
                @Index(name = "idx_relief_created_by", columnList = "created_by"),
                @Index(name = "idx_relief_rescue", columnList = "rescue_request_id")
        }
)
public class ReliefRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false,
            foreignKey = @ForeignKey(name = "fk_relief_user"))
    private UserEntity createdBy;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "DRAFT";

    @Column(name = "target_area", nullable = false, length = 255)
    private String targetArea;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rescue_request_id",
            foreignKey = @ForeignKey(name = "fk_relief_rescue"))
    private RescueRequestEntity rescueRequest;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
