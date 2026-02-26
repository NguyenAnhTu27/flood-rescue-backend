package com.floodrescue.module.rescue.repository;

import com.floodrescue.module.rescue.entity.TaskGroupEntity;
import com.floodrescue.shared.enums.TaskGroupStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskGroupRepository extends JpaRepository<TaskGroupEntity, Long> {

    Optional<TaskGroupEntity> findByCode(String code);

    Page<TaskGroupEntity> findByStatus(TaskGroupStatus status, Pageable pageable);
}
