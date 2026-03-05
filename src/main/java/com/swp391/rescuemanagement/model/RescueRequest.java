package com.swp391.rescuemanagement.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "rescue_requests")
@Getter
@Setter
@NoArgsConstructor
public class RescueRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /**
     * Người gọi/tạo yêu cầu
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caller_id", nullable = false)
    private User caller;

    /**
     * pending | in_progress | completed | cancelled
     */
    @Column(nullable = false, length = 20)
    private String status;

    /**
     * low | medium | high | critical
     */
    @Column(nullable = false, length = 20)
    private String priority;

    @Column(name = "affected_people_count")
    private Integer affectedPeopleCount;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "address_text", length = 500)
    private String addressText;

    @Column(name = "location_verified")
    private Boolean locationVerified = false;

    /**
     * Nếu đây là request chuyển/con từ request khác
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_request_id")
    private RescueRequest transferRequest;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt  = LocalDateTime.now();
        if (this.status == null)   this.status   = "pending";
        if (this.priority == null) this.priority = "medium";
        if (this.locationVerified == null) this.locationVerified = false;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
