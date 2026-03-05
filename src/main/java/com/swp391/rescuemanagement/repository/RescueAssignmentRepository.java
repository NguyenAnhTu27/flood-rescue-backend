package com.swp391.rescuemanagement.repository;

import com.swp391.rescuemanagement.model.RescueAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RescueAssignmentRepository extends JpaRepository<RescueAssignment, Long> {

    List<RescueAssignment> findByTaskGroupId(Long taskGroupId);

    /**
     * Kiểm tra team có đang trong assignment active không
     */
    @Query("""
                SELECT COUNT(a) > 0 FROM RescueAssignment a
                WHERE a.team.id = :teamId
                  AND a.taskGroup.status IN ('assigned','in_progress')
            """)
    boolean isTeamBusy(@Param("teamId") Long teamId);
}
