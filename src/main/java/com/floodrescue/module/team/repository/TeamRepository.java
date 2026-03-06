package com.floodrescue.module.team.repository;

import com.floodrescue.module.team.entity.TeamEntity;
import com.floodrescue.shared.enums.TeamStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamRepository extends JpaRepository<TeamEntity, Long> {
    List<TeamEntity> findByStatusAndLatitudeIsNotNull(TeamStatus status);
}
