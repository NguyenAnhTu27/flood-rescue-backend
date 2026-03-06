package com.floodrescue.module.team.entity;

import com.floodrescue.shared.enums.TeamStatus;
import com.floodrescue.shared.enums.TeamType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "teams", uniqueConstraints = {
        @UniqueConstraint(name = "uk_teams_code", columnNames = "code")
}, indexes = {
        @Index(name = "idx_teams_status", columnList = "status"),
        @Index(name = "idx_teams_type", columnList = "team_type")
})
public class TeamEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30, unique = true)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "team_type", nullable = false, length = 20)
    private TeamType teamType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TeamStatus status = TeamStatus.ACTIVE;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "last_location_update")
    private LocalDateTime lastLocationUpdate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
