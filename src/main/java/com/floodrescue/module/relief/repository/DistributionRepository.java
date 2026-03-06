package com.floodrescue.module.relief.repository;

import com.floodrescue.module.relief.entity.DistributionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DistributionRepository extends JpaRepository<DistributionEntity, Long> {
}
