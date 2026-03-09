package com.floodrescue.module.admin.repository;

import com.floodrescue.module.admin.entity.RolePermissionEntity;
import com.floodrescue.module.admin.entity.RolePermissionId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RolePermissionRepository extends JpaRepository<RolePermissionEntity, RolePermissionId> {
    List<RolePermissionEntity> findByRole_Code(String roleCode);
    void deleteByRole_Id(Long roleId);
}
