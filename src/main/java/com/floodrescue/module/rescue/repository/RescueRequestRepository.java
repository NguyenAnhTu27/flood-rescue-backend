package com.floodrescue.module.rescue.repository;

import com.floodrescue.module.rescue.entity.RescueRequestEntity;
import com.floodrescue.shared.enums.RescuePriority;
import com.floodrescue.shared.enums.RescueRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RescueRequestRepository extends JpaRepository<RescueRequestEntity, Long> {

    Optional<RescueRequestEntity> findByCode(String code);

    boolean existsByCode(String code);

    Page<RescueRequestEntity> findByStatus(RescueRequestStatus status, Pageable pageable);

    Page<RescueRequestEntity> findByCitizenId(Long citizenId, Pageable pageable);

    Page<RescueRequestEntity> findByStatusAndPriorityOrderByAffectedPeopleCountDescCreatedAtAsc(
            RescueRequestStatus status,
            RescuePriority priority,
            Pageable pageable
    );

    List<RescueRequestEntity> findByMasterRequestId(Long masterRequestId);

    @Query("SELECT r FROM RescueRequestEntity r WHERE r.status = :status ORDER BY r.priority DESC, r.affectedPeopleCount DESC, r.createdAt ASC")
    Page<RescueRequestEntity> findPendingRequestsOrderedByPriority(
            @Param("status") RescueRequestStatus status,
            Pageable pageable
    );
}
