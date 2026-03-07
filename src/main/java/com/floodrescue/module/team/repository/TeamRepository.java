package com.floodrescue.module.team.repository;

import com.floodrescue.module.team.entity.TeamEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<TeamEntity, Long> {
}
