package com.floodrescue.module.inventory.entity;

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
@Table(name = "inventory_receipts",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_receipt_code", columnNames = "code")
        },
        indexes = {
                @Index(name = "idx_receipt_status", columnList = "status"),
                @Index(name = "idx_receipt_created_by", columnList = "created_by")
        }
)
public class InventoryReceiptEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String code;

    @Column(name = "source_type", nullable = false, length = 20)
    private String sourceType;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "DRAFT";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false,
            foreignKey = @ForeignKey(name = "fk_receipt_user"))
    private UserEntity createdBy;

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
