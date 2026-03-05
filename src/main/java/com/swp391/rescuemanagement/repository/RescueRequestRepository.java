package com.swp391.rescuemanagement.repository;

import com.swp391.rescuemanagement.model.RescueRequest;
import com.swp391.rescuemanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RescueRequestRepository extends JpaRepository<RescueRequest, Long> {

    Optional<RescueRequest> findByCode(String code);

    /**
     * BƯỚC 2: Kiểm tra duplicate — caller đã có request 'pending' chưa?
     */
    boolean existsByCallerAndStatus(User caller, String status);

    List<RescueRequest> findByCallerOrderByCreatedAtDesc(User caller);

    List<RescueRequest> findByStatusOrderByCreatedAtDesc(String status);

    long countByCodeStartingWith(String prefix);
}
