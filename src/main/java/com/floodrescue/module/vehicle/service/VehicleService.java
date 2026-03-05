package com.floodrescue.module.vehicle.service;

import com.floodrescue.module.vehicle.dto.request.CreateVehicleRequest;
import com.floodrescue.module.vehicle.dto.request.UpdateVehicleRequest;
import com.floodrescue.module.vehicle.dto.response.VehicleResponse;
import com.floodrescue.module.vehicle.dto.response.VehicleStatisticsResponse;
import com.floodrescue.shared.enums.VehicleStatus;
import com.floodrescue.shared.enums.VehicleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface VehicleService {
    
    VehicleResponse createVehicle(CreateVehicleRequest request);
    
    VehicleResponse getVehicleById(Long id);
    
    VehicleResponse getVehicleByCode(String code);
    
    Page<VehicleResponse> getAllVehicles(Pageable pageable);
    
    List<VehicleResponse> getAvailableVehicles();
    
    List<VehicleResponse> getVehiclesByStatus(VehicleStatus status);
    
    List<VehicleResponse> getVehiclesByType(VehicleType type);
    
    List<VehicleResponse> getVehiclesByTypeAndStatus(VehicleType type, VehicleStatus status);
    
    List<VehicleResponse> getVehiclesByTeamId(Long teamId);
    
    List<VehicleResponse> getVehiclesByLocation(String location);
    
    VehicleResponse updateVehicle(Long id, UpdateVehicleRequest request);
    
    VehicleResponse assignVehicleToTeam(Long vehicleId, Long teamId);
    
    VehicleResponse updateVehicleStatus(Long vehicleId, VehicleStatus status);
    
    VehicleResponse updateVehicleLocation(Long vehicleId, String location);
    
    void deleteVehicle(Long id);
    
    VehicleStatisticsResponse getStatistics();
    
    long getAvailableVehicleCountByType(VehicleType type);
}
