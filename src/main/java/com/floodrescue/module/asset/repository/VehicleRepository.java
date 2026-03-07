package com.floodrescue.module.vehicle.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.floodrescue.module.vehicle.entity.VehicleEntity;
import com.floodrescue.shared.enums.VehicleStatus;
import com.floodrescue.shared.enums.VehicleType;

@Repository
public interface VehicleRepository extends JpaRepository<VehicleEntity, Long> {

    Optional<VehicleEntity> findByCode(String code);

    List<VehicleEntity> findByStatus(VehicleStatus status);

    List<VehicleEntity> findByVehicleType(VehicleType vehicleType);

    List<VehicleEntity> findByVehicleTypeAndStatus(VehicleType vehicleType, VehicleStatus status);

    List<VehicleEntity> findByAssignedTeamId(Long teamId);

    List<VehicleEntity> findByLocation(String location);

    @Query("SELECT v FROM VehicleEntity v WHERE v.isDeleted = false ORDER BY v.createdAt DESC")
    List<VehicleEntity> findAllActive();

    @Query("SELECT v FROM VehicleEntity v WHERE v.status = :status AND v.isDeleted = false")
    List<VehicleEntity> findAvailableByStatus(@Param("status") VehicleStatus status);

    @Query("SELECT v FROM VehicleEntity v WHERE v.vehicleType = :vehicleType AND v.status = :status AND v.isDeleted = false")
    List<VehicleEntity> findByTypeAndStatusActive(@Param("vehicleType") VehicleType vehicleType, @Param("status") VehicleStatus status);

    @Query("SELECT v FROM VehicleEntity v WHERE v.assignedTeam.id = :teamId AND v.isDeleted = false")
    List<VehicleEntity> findByTeamIdActive(@Param("teamId") Long teamId);

    @Query(value = "SELECT COUNT(*) FROM vehicles WHERE vehicle_type = :vehicleType AND status = 'AVAILABLE'", nativeQuery = true)
    long countAvailableByType(@Param("vehicleType") String vehicleType);
}
