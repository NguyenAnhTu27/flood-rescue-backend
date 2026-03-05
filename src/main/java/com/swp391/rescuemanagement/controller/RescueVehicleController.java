package com.swp391.rescuemanagement.controller;

import com.swp391.rescuemanagement.dto.request.CreateRescueVehicleRequest;
import com.swp391.rescuemanagement.dto.request.UpdateRescueVehicleRequest;
import com.swp391.rescuemanagement.dto.response.ApiResponse;
import com.swp391.rescuemanagement.dto.response.RescueVehicleResponse;
import com.swp391.rescuemanagement.service.AuthService;
import com.swp391.rescuemanagement.service.RescueVehicleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RescueVehicleController - API quản lý phương tiện cứu hộ
 * 
 * Base path: /api/rescue-vehicles
 * 
 * Endpoints:
 * - GET    /api/rescue-vehicles              - Lấy tất cả phương tiện
 * - GET    /api/rescue-vehicles/:id          - Lấy chi tiết phương tiện
 * - POST   /api/rescue-vehicles              - Tạo mới phương tiện
 * - PUT    /api/rescue-vehicles/:id          - Cập nhật phương tiện
 * - DELETE /api/rescue-vehicles/:id          - Xóa phương tiện
 * - GET    /api/rescue-vehicles/type/:type   - Tìm theo loại
 * - GET    /api/rescue-vehicles/status/:status - Tìm theo trạng thái
 * - GET    /api/rescue-vehicles/team/:teamId - Tìm theo đội
 * - PUT    /api/rescue-vehicles/:id/status   - Cập nhật trạng thái
 * - POST   /api/rescue-vehicles/:id/assign-team - Gán cho đội
 * - POST   /api/rescue-vehicles/:id/assign-dispatcher - Gán điều phối viên
 * - GET    /api/rescue-vehicles/stats        - Lấy thống kê
 */
@Slf4j
@RestController
@RequestMapping("/api/rescue-vehicles")
@RequiredArgsConstructor
public class RescueVehicleController {

    private final RescueVehicleService vehicleService;
    private final AuthService authService;

    /**
     * GET /api/rescue-vehicles
     * Lấy tất cả phương tiện cứu hộ
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<RescueVehicleResponse>>> getAll() {
        log.info("Lấy danh sách tất cả phương tiện cứu hộ");
        List<RescueVehicleResponse> vehicles = vehicleService.getAll();
        return ResponseEntity.ok(ApiResponse.ok(vehicles, "Danh sách " + vehicles.size() + " phương tiện"));
    }

    /**
     * GET /api/rescue-vehicles/{id}
     * Lấy chi tiết phương tiện theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RescueVehicleResponse>> getById(@PathVariable Long id) {
        log.info("Lấy chi tiết phương tiện ID: {}", id);
        return ResponseEntity.ok(ApiResponse.ok(
                vehicleService.getById(id),
                "Chi tiết phương tiện"));
    }

    /**
     * POST /api/rescue-vehicles
     * Tạo mới phương tiện cứu hộ
     */
    @PostMapping
    public ResponseEntity<ApiResponse<RescueVehicleResponse>> create(
            @Valid @RequestBody CreateRescueVehicleRequest request,
            HttpServletRequest httpRequest) {
        authService.getCurrentUser(httpRequest); // Phải đăng nhập
        log.info("Tạo mới phương tiện: {} ({})", request.code(), request.name());
        RescueVehicleResponse response = vehicleService.create(request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Tạo mới phương tiện thành công"));
    }

    /**
     * PUT /api/rescue-vehicles/{id}
     * Cập nhật phương tiện
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RescueVehicleResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRescueVehicleRequest request,
            HttpServletRequest httpRequest) {
        authService.getCurrentUser(httpRequest); // Phải đăng nhập
        log.info("Cập nhật phương tiện ID: {}", id);
        RescueVehicleResponse response = vehicleService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Cập nhật phương tiện thành công"));
    }

    /**
     * DELETE /api/rescue-vehicles/{id}
     * Xóa phương tiện
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> delete(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        authService.getCurrentUser(httpRequest); // Phải đăng nhập
        log.info("Xóa phương tiện ID: {}", id);
        vehicleService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("OK", "Xóa phương tiện thành công"));
    }

    /**
     * GET /api/rescue-vehicles/search/by-code?code=RV-001
     * Tìm phương tiện theo code
     */
    @GetMapping("/search/by-code")
    public ResponseEntity<ApiResponse<RescueVehicleResponse>> getByCode(@RequestParam String code) {
        log.info("Tìm phương tiện code: {}", code);
        return ResponseEntity.ok(ApiResponse.ok(
                vehicleService.getByCode(code),
                "Tìm phương tiện theo code"));
    }

    /**
     * GET /api/rescue-vehicles/search/by-type?type=rescue_person
     * Tìm phương tiện theo loại
     * 
     * Types:
     * - rescue_person (🚑) - Phương tiện cứu người
     * - transport_supplies (🚛) - Phương tiện vận chuyển hàng cứu trợ
     * - technical_support (🏗) - Phương tiện hỗ trợ kỹ thuật
     * - small_equipment (📦) - Phương tiện nhỏ / thiết bị đi kèm
     */
    @GetMapping("/search/by-type")
    public ResponseEntity<ApiResponse<List<RescueVehicleResponse>>> getByType(@RequestParam String type) {
        log.info("Tìm phương tiện loại: {}", type);
        List<RescueVehicleResponse> vehicles = vehicleService.getByVehicleType(type);
        return ResponseEntity.ok(ApiResponse.ok(vehicles, "Danh sách loại " + type));
    }

    /**
     * GET /api/rescue-vehicles/search/available-by-type?type=rescue_person
     * Tìm phương tiện có sẵn (available) theo loại
     */
    @GetMapping("/search/available-by-type")
    public ResponseEntity<ApiResponse<List<RescueVehicleResponse>>> getAvailableByType(@RequestParam String type) {
        log.info("Tìm phương tiện có sẵn loại: {}", type);
        List<RescueVehicleResponse> vehicles = vehicleService.getAvailableByType(type);
        return ResponseEntity.ok(ApiResponse.ok(vehicles, "Danh sách phương tiện " + type + " có sẵn"));
    }

    /**
     * GET /api/rescue-vehicles/search/by-status?status=available
     * Tìm phương tiện theo trạng thái
     * 
     * Status:
     * - available
     * - in_use
     * - maintenance
     * - retired
     */
    @GetMapping("/search/by-status")
    public ResponseEntity<ApiResponse<List<RescueVehicleResponse>>> getByStatus(@RequestParam String status) {
        log.info("Tìm phương tiện trạng thái: {}", status);
        List<RescueVehicleResponse> vehicles = vehicleService.getByStatus(status);
        return ResponseEntity.ok(ApiResponse.ok(vehicles, "Danh sách phương tiện trạng thái " + status));
    }

    /**
     * GET /api/rescue-vehicles/search/by-team?teamId=1
     * Tìm phương tiện được gán cho đội
     */
    @GetMapping("/search/by-team")
    public ResponseEntity<ApiResponse<List<RescueVehicleResponse>>> getByTeam(@RequestParam Long teamId) {
        log.info("Tìm phương tiện của đội ID: {}", teamId);
        List<RescueVehicleResponse> vehicles = vehicleService.getByTeam(teamId);
        return ResponseEntity.ok(ApiResponse.ok(vehicles, "Danh sách phương tiện của đội"));
    }

    /**
     * GET /api/rescue-vehicles/search/by-dispatcher?dispatcherId=2
     * Tìm phương tiện được vận hành bởi điều phối viên
     */
    @GetMapping("/search/by-dispatcher")
    public ResponseEntity<ApiResponse<List<RescueVehicleResponse>>> getByDispatcher(@RequestParam Long dispatcherId) {
        log.info("Tìm phương tiện của điều phối viên ID: {}", dispatcherId);
        List<RescueVehicleResponse> vehicles = vehicleService.getByDispatcher(dispatcherId);
        return ResponseEntity.ok(ApiResponse.ok(vehicles, "Danh sách phương tiện của điều phối viên"));
    }

    /**
     * PUT /api/rescue-vehicles/{id}/status
     * Cập nhật trạng thái phương tiện
     * 
     * Body: { "status": "available|in_use|maintenance|retired" }
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<RescueVehicleResponse>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpServletRequest httpRequest) {
        authService.getCurrentUser(httpRequest); // Phải đăng nhập
        String status = body.getOrDefault("status", "available");
        log.info("Cập nhật trạng thái phương tiện ID: {} thành: {}", id, status);
        return ResponseEntity.ok(ApiResponse.ok(
                vehicleService.updateStatus(id, status),
                "Cập nhật trạng thái thành công"));
    }

    /**
     * POST /api/rescue-vehicles/{id}/assign-team
     * Gán phương tiện cho đội
     * 
     * Body: { "teamId": 1 }
     */
    @PostMapping("/{id}/assign-team")
    public ResponseEntity<ApiResponse<RescueVehicleResponse>> assignToTeam(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body,
            HttpServletRequest httpRequest) {
        authService.getCurrentUser(httpRequest); // Phải đăng nhập
        Long teamId = body.get("teamId");
        log.info("Gán phương tiện ID: {} cho đội ID: {}", id, teamId);
        return ResponseEntity.ok(ApiResponse.ok(
                vehicleService.assignToTeam(id, teamId),
                "Gán phương tiện cho đội thành công"));
    }

    /**
     * POST /api/rescue-vehicles/{id}/assign-dispatcher
     * Gán người điều hành cho phương tiện
     * 
     * Body: { "dispatcherId": 2 }
     */
    @PostMapping("/{id}/assign-dispatcher")
    public ResponseEntity<ApiResponse<RescueVehicleResponse>> assignDispatcher(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body,
            HttpServletRequest httpRequest) {
        authService.getCurrentUser(httpRequest); // Phải đăng nhập
        Long dispatcherId = body.get("dispatcherId");
        log.info("Gán điều phối viên ID: {} cho phương tiện ID: {}", dispatcherId, id);
        return ResponseEntity.ok(ApiResponse.ok(
                vehicleService.assignDispatcher(id, dispatcherId),
                "Gán điều phối viên thành công"));
    }

    /**
     * GET /api/rescue-vehicles/stats
     * Lấy thống kê phương tiện
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        log.info("Lấy thống kê phương tiện cứu hộ");
        var stats = vehicleService.getStats();
        Map<String, Object> result = new HashMap<>();
        result.put("totalVehicles", stats.totalVehicles());
        result.put("availableCount", stats.availableCount());
        result.put("inUseCount", stats.inUseCount());
        result.put("maintenanceCount", stats.maintenanceCount());
        return ResponseEntity.ok(ApiResponse.ok(result, "Thống kê phương tiện"));
    }

    /**
     * GET /api/rescue-vehicles/types
     * Lấy danh sách các loại phương tiện
     */
    @GetMapping("/types")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getVehicleTypes() {
        log.info("Lấy danh sách loại phương tiện");
        List<Map<String, String>> types = List.of(
                Map.of("value", "rescue_person", "label", "🚑 Phương tiện cứu người"),
                Map.of("value", "transport_supplies", "label", "🚛 Phương tiện vận chuyển hàng cứu trợ"),
                Map.of("value", "technical_support", "label", "🏗 Phương tiện hỗ trợ kỹ thuật"),
                Map.of("value", "small_equipment", "label", "📦 Phương tiện nhỏ / thiết bị đi kèm")
        );
        return ResponseEntity.ok(ApiResponse.ok(types, "Danh sách loại phương tiện"));
    }

    /**
     * GET /api/rescue-vehicles/statuses
     * Lấy danh sách các trạng thái
     */
    @GetMapping("/statuses")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getStatuses() {
        log.info("Lấy danh sách trạng thái phương tiện");
        List<Map<String, String>> statuses = List.of(
                Map.of("value", "available", "label", "Có sẵn"),
                Map.of("value", "in_use", "label", "Đang sử dụng"),
                Map.of("value", "maintenance", "label", "Bảo trì"),
                Map.of("value", "retired", "label", "Đã loại biên")
        );
        return ResponseEntity.ok(ApiResponse.ok(statuses, "Danh sách trạng thái"));
    }
}
