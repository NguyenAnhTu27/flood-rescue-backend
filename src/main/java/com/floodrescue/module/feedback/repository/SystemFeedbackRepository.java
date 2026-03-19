package com.floodrescue.module.feedback.repository;

import com.floodrescue.module.feedback.entity.SystemFeedbackEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SystemFeedbackRepository extends JpaRepository<SystemFeedbackEntity, Long> {

    Page<SystemFeedbackEntity> findAllByDeletedFalse(Pageable pageable);

    Page<SystemFeedbackEntity> findByCitizenIdAndDeletedFalse(Long citizenId, Pageable pageable);

    boolean existsByIdAndDeletedFalse(Long id);

    @Query("SELECT AVG(sf.rating) FROM SystemFeedbackEntity sf WHERE sf.deleted = false")
    Double findAverageRating();

    long countByDeletedFalse();

    long countByDeletedFalseAndRating(Integer rating);
}
