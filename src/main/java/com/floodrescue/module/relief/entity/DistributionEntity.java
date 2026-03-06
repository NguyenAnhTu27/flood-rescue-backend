package com.floodrescue.module.relief.entity;

import com.floodrescue.module.inventory.entity.InventoryIssueEntity;
import com.floodrescue.module.team.entity.TeamEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "distributions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_dist_code", columnNames = "code")
        },
        indexes = {
                @Index(name = "idx_dist_status", columnList = "status"),
                @Index(name = "idx_dist_team", columnList = "assigned_team_id"),
                @Index(name = "idx_dist_relief", columnList = "relief_request_id"),
                @Index(name = "idx_dist_issue", columnList = "issue_id")
        }
)
public class DistributionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "relief_request_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_dist_relief"))
    private ReliefRequestEntity reliefRequest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assigned_team_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_dist_team"))
    private TeamEntity assignedTeam;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PLANNED";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id",
            foreignKey = @ForeignKey(name = "fk_dist_issue"))
    private InventoryIssueEntity issue;

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
