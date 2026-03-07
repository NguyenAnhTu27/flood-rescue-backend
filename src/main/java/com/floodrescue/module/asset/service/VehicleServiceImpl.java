package com.floodrescue.module.vehicle.service;

import com.floodrescue.module.team.entity.TeamEntity;
import com.floodrescue.module.team.repository.TeamRepository;
import com.floodrescue.module.vehicle.dto.request.CreateVehicleRequest;
import com.floodrescue.module.vehicle.dto.request.UpdateVehicleRequest;
import com.floodrescue.module.vehicle.dto.response.VehicleResponse;
import com.floodrescue.module.vehicle.dto.response.VehicleStatisticsResponse;
import com.floodrescue.module.vehicle.entity.VehicleEntity;
import com.floodrescue.module.vehicle.mapper.VehicleMapper;
import com.floodrescue.module.vehicle.repository.VehicleRepository;
import com.floodrescue.shared.enums.VehicleStatus;
import com.floodrescue.shared.enums.VehicleType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;
    private final TeamRepository teamRepository;

    @Override
    public VehicleResponse createVehicle(CreateVehicleRequest request) {
        log.info("Creating vehicle with code: {}", request.getCode());
        
        // Check if code already exists
        if (vehicleRepository.findByCode(request.getCode()).isPresent()) {
            throw new IllegalArgumentException("Mã phương tiện '" + request.getCode() + "' đã tồn tại");
        }

        VehicleEntity entity = vehicleMapper.toEntity(request);
        
        // Assign team if provided
        if (request.getAssignedTeamId() != null) {
            TeamEntity team = teamRepository.findById(request.getAssignedTeamId())
                    .orElseThrow(() -> new NoSuchElementException("Đội không tìm thấy với ID: " + request.getAssignedTeamId()));
            entity.setAssignedTeam(team);
        }

        VehicleEntity savedEntity = vehicleRepository.save(entity);
        log.info("Vehicle created successfully with ID: {}", savedEntity.getId());
        
        return vehicleMapper.toResponse(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleResponse getVehicleById(Long id) {
        log.info("Fetching vehicle with ID: {}", id);
        return vehicleRepository.findById(id)
                .map(vehicleMapper::toResponse)
                .orElseThrow(() -> new NoSuchElementException("Phương tiện không tìm thấy với ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleResponse getVehicleByCode(String code) {
        log.info("Fetching vehicle with code: {}", code);
        return vehicleRepository.findByCode(code)
                .map(vehicleMapper::toResponse)
                .orElseThrow(() -> new NoSuchElementException("Phương tiện không tìm thấy với mã: " + code));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VehicleResponse> getAllVehicles(Pageable pageable) {
        log.info("Fetching all vehicles with pagination: {}", pageable);
        return vehicleRepository.findAll(pageable)
                .map(vehicleMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getAvailableVehicles() {
        log.info("Fetching available vehicles");
        return vehicleRepository.findAvailableByStatus(VehicleStatus.AVAILABLE).stream()
                .map(vehicleMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getVehiclesByStatus(VehicleStatus status) {
        log.info("Fetching vehicles with status: {}", status);
        return vehicleRepository.findByStatus(status).stream()
                .map(vehicleMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getVehiclesByType(VehicleType type) {
        log.info("Fetching vehicles with type: {}", type);
        return vehicleRepository.findByVehicleType(type).stream()
                .map(vehicleMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getVehiclesByTypeAndStatus(VehicleType type, VehicleStatus status) {
        log.info("Fetching vehicles with type: {} and status: {}", type, status);
        return vehicleRepository.findByTypeAndStatusActive(type, status).stream()
                .map(vehicleMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getVehiclesByTeamId(Long teamId) {
        log.info("Fetching vehicles assigned to team: {}", teamId);
        return vehicleRepository.findByTeamIdActive(teamId).stream()
                .map(vehicleMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getVehiclesByLocation(String location) {
        log.info("Fetching vehicles at location: {}", location);
        return vehicleRepository.findByLocation(location).stream()
                .map(vehicleMapper::toResponse)
                .toList();
    }

    @Override
    public VehicleResponse updateVehicle(Long id, UpdateVehicleRequest request) {
        log.info("Updating vehicle with ID: {}", id);
        
        VehicleEntity entity = vehicleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Phương tiện không tìm thấy với ID: " + id));
        
        vehicleMapper.updateEntity(request, entity);
        
        // Update team assignment if provided
        if (request.getAssignedTeamId() != null) {
            TeamEntity team = teamRepository.findById(request.getAssignedTeamId())
                    .orElseThrow(() -> new NoSuchElementException("Đội không tìm thấy với ID: " + request.getAssignedTeamId()));
            entity.setAssignedTeam(team);
        }
        
        entity.setUpdatedAt(LocalDateTime.now());
        VehicleEntity savedEntity = vehicleRepository.save(entity);
        log.info("Vehicle updated successfully with ID: {}", id);
        
        return vehicleMapper.toResponse(savedEntity);
    }

    @Override
    public VehicleResponse assignVehicleToTeam(Long vehicleId, Long teamId) {
        log.info("Assigning vehicle {} to team {}", vehicleId, teamId);
        
        VehicleEntity vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new NoSuchElementException("Phương tiện không tìm thấy với ID: " + vehicleId));
        
        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(() -> new NoSuchElementException("Đội không tìm thấy với ID: " + teamId));
        
        vehicle.setAssignedTeam(team);
        vehicle.setUpdatedAt(LocalDateTime.now());
        VehicleEntity savedEntity = vehicleRepository.save(vehicle);
        log.info("Vehicle {} assigned to team {}", vehicleId, teamId);
        
        return vehicleMapper.toResponse(savedEntity);
    }

    @Override
    public VehicleResponse updateVehicleStatus(Long vehicleId, VehicleStatus status) {
        log.info("Updating vehicle {} status to {}", vehicleId, status);
        
        VehicleEntity vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new NoSuchElementException("Phương tiện không tìm thấy với ID: " + vehicleId));
        
        vehicle.setStatus(status);
        vehicle.setUpdatedAt(LocalDateTime.now());
        VehicleEntity savedEntity = vehicleRepository.save(vehicle);
        log.info("Vehicle {} status updated to {}", vehicleId, status);
        
        return vehicleMapper.toResponse(savedEntity);
    }

    @Override
    public VehicleResponse updateVehicleLocation(Long vehicleId, String location) {
        log.info("Updating vehicle {} location to {}", vehicleId, location);
        
        VehicleEntity vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new NoSuchElementException("Phương tiện không tìm thấy với ID: " + vehicleId));
        
        vehicle.setLocation(location);
        vehicle.setUpdatedAt(LocalDateTime.now());
        VehicleEntity savedEntity = vehicleRepository.save(vehicle);
        log.info("Vehicle {} location updated to {}", vehicleId, location);
        
        return vehicleMapper.toResponse(savedEntity);
    }

    @Override
    public void deleteVehicle(Long id) {
        log.info("Deleting vehicle with ID: {}", id);
        
        VehicleEntity entity = vehicleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Phương tiện không tìm thấy với ID: " + id));
        
        entity.setIsDeleted(true);
        entity.setUpdatedAt(LocalDateTime.now());
        vehicleRepository.save(entity);
        log.info("Vehicle deleted successfully with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleStatisticsResponse getStatistics() {
        log.info("Fetching vehicle statistics");
        
        List<VehicleEntity> allVehicles = vehicleRepository.findAllActive();
        
        VehicleStatisticsResponse response = VehicleStatisticsResponse.builder()
                .totalVehicles(allVehicles.size())
                .availableVehicles(allVehicles.stream()
                        .filter(v -> v.getStatus() == VehicleStatus.AVAILABLE)
                        .count())
                .inUseVehicles(allVehicles.stream()
                        .filter(v -> v.getStatus() == VehicleStatus.IN_USE)
                        .count())
                .maintenanceVehicles(allVehicles.stream()
                        .filter(v -> v.getStatus() == VehicleStatus.MAINTENANCE)
                        .count())
                .build();
        
        // Group by type
        var byType = allVehicles.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        VehicleEntity::getVehicleType,
                        java.util.stream.Collectors.summarizingLong(v -> 1)
                ))
                .entrySet().stream()
                .map(entry -> {
                    long total = entry.getValue().getCount();
                    long available = allVehicles.stream()
                            .filter(v -> v.getVehicleType() == entry.getKey() && v.getStatus() == VehicleStatus.AVAILABLE)
                            .count();
                    
                    return VehicleStatisticsResponse.VehicleTypeCount.builder()
                            .type(entry.getKey())
                            .typeName(entry.getKey().getVietnameseName())
                            .count(total)
                            .availableCount(available)
                            .build();
                })
                .toList();
        response.setByType(byType);
        
        // Group by status
        var byStatus = allVehicles.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        VehicleEntity::getStatus,
                        java.util.stream.Collectors.counting()
                ))
                .entrySet().stream()
                .map(entry -> VehicleStatisticsResponse.VehicleStatusCount.builder()
                        .status(entry.getKey())
                        .count(entry.getValue())
                        .build())
                .toList();
        response.setByStatus(byStatus);
        
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public long getAvailableVehicleCountByType(VehicleType type) {
        log.info("Counting available vehicles for type: {}", type);
        return vehicleRepository.countAvailableByType(type.name());
    }
}
