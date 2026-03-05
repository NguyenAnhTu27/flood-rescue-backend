package com.swp391.rescuemanagement.repository;

import com.swp391.rescuemanagement.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    List<Team> findByStatus(String status);

    /**
     * BƯỚC 3: Team RẢNH — status='active' VÀ không có TaskGroup nào đang
     * 'assigned' hoặc 'in_progress' trỏ vào team đó.
     */
    @Query("""
                SELECT t FROM Team t
                WHERE t.status = 'active'
                  AND t.id NOT IN (
                      SELECT tg.assignedTeam.id FROM TaskGroup tg
                      WHERE tg.assignedTeam IS NOT NULL
                        AND tg.status IN ('assigned','in_progress')
                  )
            """)
    List<Team> findAvailableTeams();

    /**
     * BƯỚC 3: Team ĐANG BẬN — có TaskGroup đang assigned/in_progress.
     */
    @Query("""
                SELECT DISTINCT tg.assignedTeam FROM TaskGroup tg
                WHERE tg.assignedTeam IS NOT NULL
                  AND tg.status IN ('assigned','in_progress')
            """)
    List<Team> findBusyTeams();
}
