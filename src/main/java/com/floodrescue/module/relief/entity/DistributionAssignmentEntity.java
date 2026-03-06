package com.floodrescue.module.relief.entity;

import com.floodrescue.module.asset.entity.AssetEntity;
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
@Table(name = "distribution_assignments",
        indexes = {
                @Index(name = "idx_da_dist", columnList = "distribution_id"),
                @Index(name = "idx_da_team", columnList = "team_id"),
                @Index(name = "idx_da_asset", columnList = "asset_id"),
                @Index(name = "idx_da_status", columnList = "status")
        }
)
public class DistributionAssignmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "distribution_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_da_dist"))
    private DistributionEntity distribution;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_da_team"))
    private TeamEntity team;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_da_asset"))
    private AssetEntity asset;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PLANNED";

    @Column(name = "dispatched_at")
    private LocalDateTime dispatchedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
