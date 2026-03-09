package com.floodrescue.module.admin.repository;

import com.floodrescue.module.admin.entity.AuditLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {

    void deleteByActorId(Long actorId);

    @Query("""
        SELECT a FROM AuditLogEntity a
        WHERE (:action IS NULL OR a.action = :action)
          AND (:keyword IS NULL OR LOWER(a.actor) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(COALESCE(a.target, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(COALESCE(a.detail, '')) LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY a.createdAt DESC
    """)
    Page<AuditLogEntity> search(
            @Param("action") String action,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
