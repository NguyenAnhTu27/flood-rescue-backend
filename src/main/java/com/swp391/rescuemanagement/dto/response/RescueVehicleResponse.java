package com.swp391.rescuemanagement.dto.response;

import com.swp391.rescuemanagement.model.RescueVehicle;

import java.time.LocalDateTime;

/**
 * DTO Response cho RescueVehicle
 * Java 21 Record pattern
 */
public record RescueVehicleResponse(
        Long id,
        String code,
        String name,
        String vehicleType,
        String icon,
        String description,
        String status,
        Integer capacity,
        Long dispatcherId,
        String dispatcherName,
        Long assignedTeamId,
        String assignedTeamName,
        String licensePlate,
        String contactNumber,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * Convert RescueVehicle entity to RescueVehicleResponse
     */
    public static RescueVehicleResponse from(RescueVehicle vehicle) {
        return new RescueVehicleResponse(
                vehicle.getId(),
                vehicle.getCode(),
                vehicle.getName(),
                vehicle.getVehicleType(),
                vehicle.getIcon(),
                vehicle.getDescription(),
                vehicle.getStatus(),
                vehicle.getCapacity(),
                vehicle.getDispatcher() != null ? vehicle.getDispatcher().getId() : null,
                vehicle.getDispatcher() != null ? vehicle.getDispatcher().getFullName() : null,
                vehicle.getAssignedTeam() != null ? vehicle.getAssignedTeam().getId() : null,
                vehicle.getAssignedTeam() != null ? vehicle.getAssignedTeam().getName() : null,
                vehicle.getLicensePlate(),
                vehicle.getContactNumber(),
                vehicle.getCreatedAt(),
                vehicle.getUpdatedAt()
        );
    }
}
