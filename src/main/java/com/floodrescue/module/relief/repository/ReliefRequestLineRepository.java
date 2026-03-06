package com.floodrescue.module.relief.repository;

import com.floodrescue.module.relief.entity.ReliefRequestLineEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReliefRequestLineRepository extends JpaRepository<ReliefRequestLineEntity, Long> {
}
