package com.floodrescue.module.team.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.floodrescue.module.team.entity.TeamEntity;

public interface TeamRepository extends JpaRepository<TeamEntity, Long> {
    boolean existsByCode(String code);
    Optional<TeamEntity> findByCode(String code);

    boolean existsByName(String name);
    Optional<TeamEntity> findByName(String name);
    List<TeamEntity> findByStatusAndCurrentLatitudeIsNotNull(Byte status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update TeamEntity t
               set t.currentLatitude = :latitude,
                   t.currentLongitude = :longitude,
                   t.currentLocationText = :locationText,
                   t.currentLocationUpdatedAt = :updatedAt
             where t.id = :teamId
            """)
    int updateCurrentLocation(
            @Param("teamId") Long teamId,
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("locationText") String locationText,
            @Param("updatedAt") LocalDateTime updatedAt
    );
}
