package com.floodrescue.module.vehicle.entity;

import java.time.LocalDateTime;

import com.floodrescue.module.team.entity.TeamEntity;
import com.floodrescue.shared.enums.VehicleStatus;
import com.floodrescue.shared.enums.VehicleType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "vehicles",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_vehicles_code", columnNames = "code")
        },
        indexes = {
                @Index(name = "idx_vehicles_status", columnList = "status"),
                @Index(name = "idx_vehicles_type", columnList = "vehicle_type"),
                @Index(name = "idx_vehicles_team", columnList = "assigned_team_id"),
                @Index(name = "idx_vehicles_location", columnList = "location")
        }
)
public class VehicleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30, unique = true)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false, length = 50)
    private VehicleType vehicleType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private VehicleStatus status = VehicleStatus.AVAILABLE;

    @Column
    private Integer capacity;

    @Column(length = 255)
    private String location;

    @Column(name = "license_plate", length = 20)
    private String licensePlate;

    @Column(name = "vin_number", length = 50)
    private String vinNumber;

    @Column(name = "last_maintenance_date")
    private LocalDateTime lastMaintenanceDate;

    @Column(name = "next_maintenance_date")
    private LocalDateTime nextMaintenanceDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "contact_number", length = 20)
    private String contactNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_team_id",
            foreignKey = @ForeignKey(name = "fk_vehicles_team"))
    private TeamEntity assignedTeam;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;
}
