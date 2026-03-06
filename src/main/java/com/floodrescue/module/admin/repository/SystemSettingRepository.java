package com.floodrescue.module.admin.repository;

import com.floodrescue.module.admin.entity.SystemSettingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SystemSettingRepository extends JpaRepository<SystemSettingEntity, Long> {
    List<SystemSettingEntity> findAllByOrderBySettingKeyAsc();
    Optional<SystemSettingEntity> findBySettingKey(String settingKey);
    Optional<SystemSettingEntity> findByKeyName(String keyName);
}
