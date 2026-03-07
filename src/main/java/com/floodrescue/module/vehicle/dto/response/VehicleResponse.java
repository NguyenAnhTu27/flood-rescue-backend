package com.floodrescue.module.vehicle.dto.response;

import java.time.LocalDateTime;

import com.floodrescue.shared.enums.VehicleStatus;
import com.floodrescue.shared.enums.VehicleType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleResponse {
    private Long id;
    private String code;
    private String name;
    private VehicleType vehicleType;
    private String vehicleTypeDisplay;
    private VehicleStatus status;
    private Integer capacity;
    private String location;
    private String licensePlate;
    private String vinNumber;
    private LocalDateTime lastMaintenanceDate;
    private LocalDateTime nextMaintenanceDate;
    private String description;
    private String contactNumber;
    private Long assignedTeamId;
    private String assignedTeamName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
