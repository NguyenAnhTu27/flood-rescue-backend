package com.floodrescue.module.admin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "system_settings", uniqueConstraints = {
        @UniqueConstraint(name = "uk_system_settings_key", columnNames = "setting_key")
})
public class SystemSettingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "key_name", nullable = false, length = 120)
    private String keyName;

    @Column(name = "value_text", nullable = false, columnDefinition = "TEXT")
    private String valueText;

    @Column(name = "value_type", nullable = false, length = 10)
    private String valueType;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "setting_key", nullable = false, length = 100)
    private String settingKey;

    @Column(name = "setting_value", nullable = false, length = 500)
    private String settingValue;

    @Column(length = 255)
    private String description;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
