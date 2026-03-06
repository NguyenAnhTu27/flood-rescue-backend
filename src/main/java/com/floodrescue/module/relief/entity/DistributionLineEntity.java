package com.floodrescue.module.relief.entity;

import com.floodrescue.module.inventory.entity.ItemCategoryEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "distribution_lines",
        indexes = {
                @Index(name = "idx_dline_dist", columnList = "distribution_id"),
                @Index(name = "idx_dline_item", columnList = "item_category_id")
        }
)
public class DistributionLineEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "distribution_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_dline_dist"))
    private DistributionEntity distribution;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_category_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_dline_item"))
    private ItemCategoryEntity itemCategory;

    @Column(nullable = false, precision = 14, scale = 2)
    private java.math.BigDecimal qty;

    @Column(nullable = false, length = 20)
    private String unit;
}
