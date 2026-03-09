package com.floodrescue.module.admin.entity;

import com.floodrescue.module.user.entity.RoleEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "role_permissions")
@IdClass(RolePermissionId.class)
public class RolePermissionEntity {

    @Id
    @Column(name = "role_id")
    private Long roleId;

    @Id
    @Column(name = "permission_id")
    private Long permissionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false, insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_rp_role"))
    private RoleEntity role;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "permission_id", nullable = false, insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_rp_perm"))
    private PermissionEntity permission;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
