package com.swp391.rescuemanagement.repository;

import com.swp391.rescuemanagement.model.RescueVehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RescueVehicleRepository extends JpaRepository<RescueVehicle, Long> {

    /**
     * Tìm phương tiện theo code
     */
    Optional<RescueVehicle> findByCode(String code);

    /**
     * Tìm phương tiện theo loại
     */
    List<RescueVehicle> findByVehicleType(String vehicleType);

    /**
     * Tìm phương tiện theo trạng thái
     */
    List<RescueVehicle> findByStatus(String status);

    /**
     * Tìm phương tiện được gán cho đội
     */
    List<RescueVehicle> findByAssignedTeamId(Long teamId);

    /**
     * Tìm phương tiện được vận hành bởi điều phối viên
     */
    List<RescueVehicle> findByDispatcherId(Long dispatcherId);

    /**
     * Tìm phương tiện có sẵn theo loại
     */
    List<RescueVehicle> findByVehicleTypeAndStatus(String vehicleType, String status);

    /**
     * Tìm tất cả phương tiện có sẵn
     */
    List<RescueVehicle> findByStatus(String status);

    /**
     * Query tìm phương tiện có sẵn (available) được gán cho đội hoặc không gán cho ai
     */
    @Query("SELECT rv FROM RescueVehicle rv " +
            "WHERE rv.status = 'available' " +
            "AND (rv.assignedTeamId = :teamId OR rv.assignedTeamId IS NULL) " +
            "ORDER BY rv.vehicleType")
    List<RescueVehicle> findAvailableVehiclesForTeam(@Param("teamId") Long teamId);

    /**
     * Đếm số phương tiện theo loại và trạng thái
     */
    long countByVehicleTypeAndStatus(String vehicleType, String status);
}
