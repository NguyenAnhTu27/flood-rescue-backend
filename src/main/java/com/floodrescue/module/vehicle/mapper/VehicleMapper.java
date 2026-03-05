package com.floodrescue.module.vehicle.mapper;

import com.floodrescue.module.vehicle.dto.request.CreateVehicleRequest;
import com.floodrescue.module.vehicle.dto.request.UpdateVehicleRequest;
import com.floodrescue.module.vehicle.dto.response.VehicleResponse;
import com.floodrescue.module.vehicle.entity.VehicleEntity;
import org.springframework.stereotype.Component;

@Component
public class VehicleMapper {

    public VehicleResponse toResponse(VehicleEntity entity) {
        if (entity == null) {
            return null;
        }

        VehicleResponse.VehicleResponseBuilder builder = VehicleResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .vehicleType(entity.getVehicleType())
                .vehicleTypeDisplay(entity.getVehicleType() != null ? entity.getVehicleType().getVietnameseName() : null)
                .status(entity.getStatus())
                .capacity(entity.getCapacity())
                .location(entity.getLocation())
                .licensePlate(entity.getLicensePlate())
                .vinNumber(entity.getVinNumber())
                .lastMaintenanceDate(entity.getLastMaintenanceDate())
                .nextMaintenanceDate(entity.getNextMaintenanceDate())
                .description(entity.getDescription())
                .contactNumber(entity.getContactNumber())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());

        if (entity.getAssignedTeam() != null) {
            builder.assignedTeamId(entity.getAssignedTeam().getId())
                    .assignedTeamName(entity.getAssignedTeam().getName());
        }

        return builder.build();
    }

    public VehicleEntity toEntity(CreateVehicleRequest request) {
        if (request == null) {
            return null;
        }

        return VehicleEntity.builder()
                .code(request.getCode())
                .name(request.getName())
                .vehicleType(request.getVehicleType())
                .capacity(request.getCapacity())
                .location(request.getLocation())
                .licensePlate(request.getLicensePlate())
                .vinNumber(request.getVinNumber())
                .lastMaintenanceDate(request.getLastMaintenanceDate())
                .nextMaintenanceDate(request.getNextMaintenanceDate())
                .description(request.getDescription())
                .contactNumber(request.getContactNumber())
                .build();
    }

    public void updateEntity(UpdateVehicleRequest request, VehicleEntity entity) {
        if (request == null || entity == null) {
            return;
        }

        entity.setName(request.getName());
        entity.setVehicleType(request.getVehicleType());
        entity.setStatus(request.getStatus());
        entity.setCapacity(request.getCapacity());
        entity.setLocation(request.getLocation());
        entity.setLicensePlate(request.getLicensePlate());
        entity.setVinNumber(request.getVinNumber());
        entity.setLastMaintenanceDate(request.getLastMaintenanceDate());
        entity.setNextMaintenanceDate(request.getNextMaintenanceDate());
        entity.setDescription(request.getDescription());
        entity.setContactNumber(request.getContactNumber());
    }
}
