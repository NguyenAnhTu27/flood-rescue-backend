package com.floodrescue.module.relief.repository;

import com.floodrescue.module.relief.entity.ReliefRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReliefRequestRepository extends JpaRepository<ReliefRequestEntity, Long> {
}
