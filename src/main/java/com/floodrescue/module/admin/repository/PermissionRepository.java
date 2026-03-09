package com.floodrescue.module.admin.repository;

import com.floodrescue.module.admin.entity.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermissionRepository extends JpaRepository<PermissionEntity, Long> {
    List<PermissionEntity> findAllByOrderByIdAsc();
}
