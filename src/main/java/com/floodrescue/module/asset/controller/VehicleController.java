package com.floodrescue.module.vehicle.controller;

import com.floodrescue.module.vehicle.dto.request.CreateVehicleRequest;
import com.floodrescue.module.vehicle.dto.request.UpdateVehicleRequest;
import com.floodrescue.module.vehicle.dto.response.VehicleResponse;
import com.floodrescue.module.vehicle.dto.response.VehicleStatisticsResponse;
import com.floodrescue.module.vehicle.service.VehicleService;
import com.floodrescue.shared.enums.VehicleStatus;
import com.floodrescue.shared.enums.VehicleType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
@Slf4j
public class VehicleController {

    private final VehicleService vehicleService;

    /**
     * Tạo phương tiện mới
     * Yêu cầu roles: MANAGER, RESCUE_COORDINATOR
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'RESCUE_COORDINATOR')")
    public ResponseEntity<VehicleResponse> createVehicle(@Valid @RequestBody CreateVehicleRequest request) {
        log.info("POST /api/v1/vehicles - Create vehicle: {}", request.getName());
        VehicleResponse response = vehicleService.createVehicle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Lấy danh sách tất cả phương tiện (có phân trang)
     */
    @GetMapping
    public ResponseEntity<Page<VehicleResponse>> getAllVehicles(Pageable pageable) {
        log.info("GET /api/v1/vehicles - Get all vehicles");
        Page<VehicleResponse> response = vehicleService.getAllVehicles(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy chi tiết phương tiện theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponse> getVehicleById(@PathVariable Long id) {
        log.info("GET /api/v1/vehicles/{} - Get vehicle by ID", id);
        VehicleResponse response = vehicleService.getVehicleById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy phương tiện theo mã code
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<VehicleResponse> getVehicleByCode(@PathVariable String code) {
        log.info("GET /api/v1/vehicles/code/{} - Get vehicle by code", code);
        VehicleResponse response = vehicleService.getVehicleByCode(code);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy danh sách phương tiện có sẵn
     */
    @GetMapping("/available")
    public ResponseEntity<List<VehicleResponse>> getAvailableVehicles() {
        log.info("GET /api/v1/vehicles/available - Get available vehicles");
        List<VehicleResponse> response = vehicleService.getAvailableVehicles();
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy danh sách phương tiện theo trạng thái
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<VehicleResponse>> getVehiclesByStatus(@PathVariable VehicleStatus status) {
        log.info("GET /api/v1/vehicles/status/{} - Get vehicles by status", status);
        List<VehicleResponse> response = vehicleService.getVehiclesByStatus(status);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy danh sách phương tiện theo loại
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<VehicleResponse>> getVehiclesByType(@PathVariable VehicleType type) {
        log.info("GET /api/v1/vehicles/type/{} - Get vehicles by type", type);
        List<VehicleResponse> response = vehicleService.getVehiclesByType(type);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy danh sách phương tiện theo loại và trạng thái
     */
    @GetMapping("/type/{type}/status/{status}")
    public ResponseEntity<List<VehicleResponse>> getVehiclesByTypeAndStatus(
            @PathVariable VehicleType type,
            @PathVariable VehicleStatus status) {
        log.info("GET /api/v1/vehicles/type/{}/status/{}", type, status);
        List<VehicleResponse> response = vehicleService.getVehiclesByTypeAndStatus(type, status);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy danh sách phương tiện được gán cho đội
     */
    @GetMapping("/team/{teamId}")
    public ResponseEntity<List<VehicleResponse>> getVehiclesByTeamId(@PathVariable Long teamId) {
        log.info("GET /api/v1/vehicles/team/{} - Get vehicles by team", teamId);
        List<VehicleResponse> response = vehicleService.getVehiclesByTeamId(teamId);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy danh sách phương tiện theo địa điểm
     */
    @GetMapping("/location/{location}")
    public ResponseEntity<List<VehicleResponse>> getVehiclesByLocation(@PathVariable String location) {
        log.info("GET /api/v1/vehicles/location/{} - Get vehicles by location", location);
        List<VehicleResponse> response = vehicleService.getVehiclesByLocation(location);
        return ResponseEntity.ok(response);
    }

    /**
     * Cập nhật thông tin phương tiện
     * Yêu cầu roles: MANAGER, RESCUE_COORDINATOR
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'RESCUE_COORDINATOR')")
    public ResponseEntity<VehicleResponse> updateVehicle(
            @PathVariable Long id,
            @Valid @RequestBody UpdateVehicleRequest request) {
        log.info("PUT /api/v1/vehicles/{} - Update vehicle", id);
        VehicleResponse response = vehicleService.updateVehicle(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Gán phương tiện cho đội
     * Yêu cầu roles: MANAGER, RESCUE_COORDINATOR
     */
    @PostMapping("/{vehicleId}/assign-team/{teamId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'RESCUE_COORDINATOR')")
    public ResponseEntity<VehicleResponse> assignVehicleToTeam(
            @PathVariable Long vehicleId,
            @PathVariable Long teamId) {
        log.info("POST /api/v1/vehicles/{}/assign-team/{}", vehicleId, teamId);
        VehicleResponse response = vehicleService.assignVehicleToTeam(vehicleId, teamId);
        return ResponseEntity.ok(response);
    }

    /**
     * Cập nhật trạng thái phương tiện
     * Yêu cầu roles: MANAGER, RESCUE_COORDINATOR
     */
    @PatchMapping("/{vehicleId}/status/{status}")
    @PreAuthorize("hasAnyRole('MANAGER', 'RESCUE_COORDINATOR')")
    public ResponseEntity<VehicleResponse> updateVehicleStatus(
            @PathVariable Long vehicleId,
            @PathVariable VehicleStatus status) {
        log.info("PATCH /api/v1/vehicles/{}/status/{}", vehicleId, status);
        VehicleResponse response = vehicleService.updateVehicleStatus(vehicleId, status);
        return ResponseEntity.ok(response);
    }

    /**
     * Cập nhật vị trí phương tiện
     * Yêu cầu roles: MANAGER, RESCUE_COORDINATOR
     */
    @PatchMapping("/{vehicleId}/location")
    @PreAuthorize("hasAnyRole('MANAGER', 'RESCUE_COORDINATOR')")
    public ResponseEntity<VehicleResponse> updateVehicleLocation(
            @PathVariable Long vehicleId,
            @RequestParam String location) {
        log.info("PATCH /api/v1/vehicles/{}/location - Update location to {}", vehicleId, location);
        VehicleResponse response = vehicleService.updateVehicleLocation(vehicleId, location);
        return ResponseEntity.ok(response);
    }

    /**
     * Xóa phương tiện (soft delete)
     * Yêu cầu roles: MANAGER
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        log.info("DELETE /api/v1/vehicles/{} - Delete vehicle", id);
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lấy thống kê phương tiện
     */
    @GetMapping("/statistics")
    public ResponseEntity<VehicleStatisticsResponse> getStatistics() {
        log.info("GET /api/v1/vehicles/statistics - Get vehicle statistics");
        VehicleStatisticsResponse response = vehicleService.getStatistics();
        return ResponseEntity.ok(response);
    }

    /**
     * Đếm số lượng phương tiện có sẵn theo loại
     */
    @GetMapping("/count/available")
    public ResponseEntity<Long> getAvailableVehicleCount(@RequestParam VehicleType type) {
        log.info("GET /api/v1/vehicles/count/available - Count available vehicles for type: {}", type);
        long count = vehicleService.getAvailableVehicleCountByType(type);
        return ResponseEntity.ok(count);
    }
}
