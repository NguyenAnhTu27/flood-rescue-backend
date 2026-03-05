package com.swp391.rescuemanagement.repository;

import com.swp391.rescuemanagement.model.TaskGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskGroupRepository extends JpaRepository<TaskGroup, Long> {
    Optional<TaskGroup> findByCode(String code);

    List<TaskGroup> findByAssignedTeamIdAndStatusIn(Long teamId, List<String> statuses);

    long countByCodeStartingWith(String prefix);
}
