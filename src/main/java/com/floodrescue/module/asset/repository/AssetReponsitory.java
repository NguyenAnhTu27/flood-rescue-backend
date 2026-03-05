package com.floodrescue.module.asset.repository;

import com.floodrescue.module.asset.entity.AssetEntity;
import com.floodrescue.shared.enums.AssetStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetReponsitory extends JpaRepository<AssetEntity, Long> {

    List<AssetEntity> findByStatus(AssetStatus status);

    List<AssetEntity> findByAssignedTeamIdAndStatus(Long teamId, AssetStatus status);
}
