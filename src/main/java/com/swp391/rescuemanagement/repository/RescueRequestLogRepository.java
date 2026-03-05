package com.swp391.rescuemanagement.repository;

import com.swp391.rescuemanagement.model.RescueRequestLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RescueRequestLogRepository extends JpaRepository<RescueRequestLog, Long> {
    List<RescueRequestLog> findByRescueRequestIdOrderByCreatedAtAsc(Long requestId);
}
