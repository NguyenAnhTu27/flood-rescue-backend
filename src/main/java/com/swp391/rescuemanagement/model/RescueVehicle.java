package com.swp391.rescuemanagement.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * RescueVehicle - Phương tiện cứu hộ do điều phối viên thực hiện
 * 
 * 4 loại phương tiện:
 * 1. rescue_person (🚑) - Phương tiện cứu người
 * 2. transport_supplies (🚛) - Phương tiện vận chuyển hàng cứu trợ
 * 3. technical_support (🏗) - Phương tiện hỗ trợ kỹ thuật
 * 4. small_equipment (📦) - Phương tiện nhỏ / thiết bị đi kèm
 */
@Entity
@Table(name = "rescue_vehicles")
@Getter
@Setter
@NoArgsConstructor
public class RescueVehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code; // RV-001, RV-002, ...

    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Loại phương tiện:
     * rescue_person | transport_supplies | technical_support | small_equipment
     */
    @Column(name = "vehicle_type", nullable = false, length = 50)
    private String vehicleType;

    /**
     * Icon emoji: 🚑 | 🚛 | 🏗 | 📦
     */
    @Column(nullable = true, length = 10)
    private String icon;

    /**
     * Mô tả chi tiết về phương tiện
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Trạng thái:
     * available | in_use | maintenance | retired
     */
    @Column(nullable = false, length = 20)
    private String status;

    /**
     * Sức chứa: số lượng người hoặc kg
     */
    @Column(nullable = true)
    private Integer capacity;

    /**
     * FK → users.id (Điều phối viên vận hành phương tiện này)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispatcher_id")
    private User dispatcher;

    /**
     * FK → teams.id (Đội được gán phương tiện)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_team_id")
    private Team assignedTeam;

    /**
     * Biển số xe
     */
    @Column(name = "license_plate", length = 20)
    private String licensePlate;

    /**
     * Số điện thoại liên hệ
     */
    @Column(name = "contact_number", length = 20)
    private String contactNumber;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) this.status = "available";
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
