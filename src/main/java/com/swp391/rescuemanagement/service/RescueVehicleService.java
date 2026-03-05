package com.swp391.rescuemanagement.service;

import com.swp391.rescuemanagement.dto.request.CreateRescueVehicleRequest;
import com.swp391.rescuemanagement.dto.request.UpdateRescueVehicleRequest;
import com.swp391.rescuemanagement.dto.response.RescueVehicleResponse;
import com.swp391.rescuemanagement.exception.BusinessException;
import com.swp391.rescuemanagement.model.RescueVehicle;
import com.swp391.rescuemanagement.model.Team;
import com.swp391.rescuemanagement.model.User;
import com.swp391.rescuemanagement.repository.RescueVehicleRepository;
import com.swp391.rescuemanagement.repository.TeamRepository;
import com.swp391.rescuemanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * RescueVehicleService - Quản lý phương tiện cứu hộ
 * 
 * Tính năng:
 * - CRUD phương tiện
 * - Tìm kiếm theo loại, trạng thái, đội, điều phối viên
 * - Cập nhật trạng thái và người điều hành
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RescueVehicleService {

    private final RescueVehicleRepository vehicleRepo;
    private final UserRepository userRepo;
    private final TeamRepository teamRepo;

    /**
     * Tạo mới phương tiện cứu hộ
     */
    @Transactional
    public RescueVehicleResponse create(CreateRescueVehicleRequest request) {
        // Kiểm tra code không bị trùng
        if (vehicleRepo.findByCode(request.code()).isPresent()) {
            throw new BusinessException("Code phương tiện '" + request.code() + "' đã tồn tại");
        }

        RescueVehicle vehicle = new RescueVehicle();
        vehicle.setCode(request.code());
        vehicle.setName(request.name());
        vehicle.setVehicleType(request.vehicleType());
        vehicle.setIcon(request.icon());
        vehicle.setDescription(request.description());
        vehicle.setStatus(request.status());
        vehicle.setCapacity(request.capacity());
        vehicle.setLicensePlate(request.licensePlate());
        vehicle.setContactNumber(request.contactNumber());

        // Set dispatcher nếu có
        if (request.dispatcherId() != null) {
            User dispatcher = userRepo.findById(request.dispatcherId())
                    .orElseThrow(() -> BusinessException.notFound("user", request.dispatcherId()));
            vehicle.setDispatcher(dispatcher);
        }

        // Set team nếu có
        if (request.assignedTeamId() != null) {
            Team team = teamRepo.findById(request.assignedTeamId())
                    .orElseThrow(() -> BusinessException.notFound("team", request.assignedTeamId()));
            vehicle.setAssignedTeam(team);
        }

        RescueVehicle saved = vehicleRepo.save(vehicle);
        log.info("Tạo mới phương tiện cứu hộ: {} ({})", saved.getCode(), saved.getName());

        return RescueVehicleResponse.from(saved);
    }

    /**
     * Lấy tất cả phương tiện
     */
    @Transactional(readOnly = true)
    public List<RescueVehicleResponse> getAll() {
        return vehicleRepo.findAll().stream()
                .map(RescueVehicleResponse::from)
                .toList();
    }

    /**
     * Lấy phương tiện theo ID
     */
    @Transactional(readOnly = true)
    public RescueVehicleResponse getById(Long id) {
        RescueVehicle vehicle = vehicleRepo.findById(id)
                .orElseThrow(() -> BusinessException.notFound("rescue_vehicle", id));
        return RescueVehicleResponse.from(vehicle);
    }

    /**
     * Tìm phương tiện theo code
     */
    @Transactional(readOnly = true)
    public RescueVehicleResponse getByCode(String code) {
        RescueVehicle vehicle = vehicleRepo.findByCode(code)
                .orElseThrow(() -> new BusinessException("Không tìm thấy phương tiện với code: " + code));
        return RescueVehicleResponse.from(vehicle);
    }

    /**
     * Tìm phương tiện theo loại
     */
    @Transactional(readOnly = true)
    public List<RescueVehicleResponse> getByVehicleType(String vehicleType) {
        List<RescueVehicle> vehicles = vehicleRepo.findByVehicleType(vehicleType);
        log.debug("Tìm phương tiện loại {}: {} chiếc", vehicleType, vehicles.size());
        return vehicles.stream().map(RescueVehicleResponse::from).toList();
    }

    /**
     * Tìm phương tiện theo trạng thái
     */
    @Transactional(readOnly = true)
    public List<RescueVehicleResponse> getByStatus(String status) {
        List<RescueVehicle> vehicles = vehicleRepo.findByStatus(status);
        log.debug("Tìm phương tiện trạng thái {}: {} chiếc", status, vehicles.size());
        return vehicles.stream().map(RescueVehicleResponse::from).toList();
    }

    /**
     * Tìm phương tiện được gán cho đội
     */
    @Transactional(readOnly = true)
    public List<RescueVehicleResponse> getByTeam(Long teamId) {
        Team team = teamRepo.findById(teamId)
                .orElseThrow(() -> BusinessException.notFound("team", teamId));
        List<RescueVehicle> vehicles = vehicleRepo.findByAssignedTeamId(teamId);
        log.debug("Đội {} có {} phương tiện", team.getName(), vehicles.size());
        return vehicles.stream().map(RescueVehicleResponse::from).toList();
    }

    /**
     * Tìm phương tiện được vận hành bởi điều phối viên
     */
    @Transactional(readOnly = true)
    public List<RescueVehicleResponse> getByDispatcher(Long dispatcherId) {
        User dispatcher = userRepo.findById(dispatcherId)
                .orElseThrow(() -> BusinessException.notFound("user", dispatcherId));
        List<RescueVehicle> vehicles = vehicleRepo.findByDispatcherId(dispatcherId);
        log.debug("Điều phối viên {} vận hành {} phương tiện", dispatcher.getFullName(), vehicles.size());
        return vehicles.stream().map(RescueVehicleResponse::from).toList();
    }

    /**
     * Lấy phương tiện có sẵn (available) theo loại
     */
    @Transactional(readOnly = true)
    public List<RescueVehicleResponse> getAvailableByType(String vehicleType) {
        List<RescueVehicle> vehicles = vehicleRepo.findByVehicleTypeAndStatus(vehicleType, "available");
        log.debug("Phương tiện loại {} có sẵn: {} chiếc", vehicleType, vehicles.size());
        return vehicles.stream().map(RescueVehicleResponse::from).toList();
    }

    /**
     * Cập nhật phương tiện
     */
    @Transactional
    public RescueVehicleResponse update(Long id, UpdateRescueVehicleRequest request) {
        RescueVehicle vehicle = vehicleRepo.findById(id)
                .orElseThrow(() -> BusinessException.notFound("rescue_vehicle", id));

        if (request.name() != null) vehicle.setName(request.name());
        if (request.description() != null) vehicle.setDescription(request.description());
        if (request.status() != null) vehicle.setStatus(request.status());
        if (request.capacity() != null) vehicle.setCapacity(request.capacity());
        if (request.licensePlate() != null) vehicle.setLicensePlate(request.licensePlate());
        if (request.contactNumber() != null) vehicle.setContactNumber(request.contactNumber());

        // Cập nhật dispatcher
        if (request.dispatcherId() != null) {
            User dispatcher = userRepo.findById(request.dispatcherId())
                    .orElseThrow(() -> BusinessException.notFound("user", request.dispatcherId()));
            vehicle.setDispatcher(dispatcher);
        }

        // Cập nhật team
        if (request.assignedTeamId() != null) {
            Team team = teamRepo.findById(request.assignedTeamId())
                    .orElseThrow(() -> BusinessException.notFound("team", request.assignedTeamId()));
            vehicle.setAssignedTeam(team);
        }

        RescueVehicle updated = vehicleRepo.save(vehicle);
        log.info("Cập nhật phương tiện cứu hộ: {} ({})", updated.getCode(), updated.getName());

        return RescueVehicleResponse.from(updated);
    }

    /**
     * Cập nhật trạng thái phương tiện
     */
    @Transactional
    public RescueVehicleResponse updateStatus(Long id, String newStatus) {
        RescueVehicle vehicle = vehicleRepo.findById(id)
                .orElseThrow(() -> BusinessException.notFound("rescue_vehicle", id));
        vehicle.setStatus(newStatus);
        RescueVehicle updated = vehicleRepo.save(vehicle);
        log.info("Cập nhật trạng thái phương tiện {} thành: {}", vehicle.getCode(), newStatus);
        return RescueVehicleResponse.from(updated);
    }

    /**
     * Gán phương tiện cho đội
     */
    @Transactional
    public RescueVehicleResponse assignToTeam(Long vehicleId, Long teamId) {
        RescueVehicle vehicle = vehicleRepo.findById(vehicleId)
                .orElseThrow(() -> BusinessException.notFound("rescue_vehicle", vehicleId));
        Team team = teamRepo.findById(teamId)
                .orElseThrow(() -> BusinessException.notFound("team", teamId));
        vehicle.setAssignedTeam(team);
        RescueVehicle updated = vehicleRepo.save(vehicle);
        log.info("Gán phương tiện {} cho đội: {}", vehicle.getCode(), team.getName());
        return RescueVehicleResponse.from(updated);
    }

    /**
     * Gán người điều hành cho phương tiện
     */
    @Transactional
    public RescueVehicleResponse assignDispatcher(Long vehicleId, Long dispatcherId) {
        RescueVehicle vehicle = vehicleRepo.findById(vehicleId)
                .orElseThrow(() -> BusinessException.notFound("rescue_vehicle", vehicleId));
        User dispatcher = userRepo.findById(dispatcherId)
                .orElseThrow(() -> BusinessException.notFound("user", dispatcherId));
        vehicle.setDispatcher(dispatcher);
        RescueVehicle updated = vehicleRepo.save(vehicle);
        log.info("Gán điều phối viên {} cho phương tiện: {}", dispatcher.getFullName(), vehicle.getCode());
        return RescueVehicleResponse.from(updated);
    }

    /**
     * Xóa phương tiện
     */
    @Transactional
    public void delete(Long id) {
        RescueVehicle vehicle = vehicleRepo.findById(id)
                .orElseThrow(() -> BusinessException.notFound("rescue_vehicle", id));
        vehicleRepo.delete(vehicle);
        log.info("Xóa phương tiện cứu hộ: {} ({})", vehicle.getCode(), vehicle.getName());
    }

    /**
     * Lấy thống kê phương tiện
     */
    @Transactional(readOnly = true)
    public RescueVehicleStats getStats() {
        long total = vehicleRepo.count();
        long available = vehicleRepo.countByVehicleTypeAndStatus("rescue_person", "available");
        List<RescueVehicle> all = vehicleRepo.findAll();

        long rescuePersonCount = vehicleRepo.countByVehicleTypeAndStatus("rescue_person", "available");
        long transportCount = vehicleRepo.countByVehicleTypeAndStatus("transport_supplies", "available");
        long technicalCount = vehicleRepo.countByVehicleTypeAndStatus("technical_support", "available");
        long smallEquipmentCount = vehicleRepo.countByVehicleTypeAndStatus("small_equipment", "available");

        return new RescueVehicleStats(
                total,
                (long) all.stream().filter(v -> "available".equals(v.getStatus())).count(),
                (long) all.stream().filter(v -> "in_use".equals(v.getStatus())).count(),
                (long) all.stream().filter(v -> "maintenance".equals(v.getStatus())).count()
        );
    }

    /**
     * Record for statistics
     */
    public record RescueVehicleStats(
            long totalVehicles,
            long availableCount,
            long inUseCount,
            long maintenanceCount
    ) {
    }
}
