package com.swp391.rescuemanagement.controller;

import com.swp391.rescuemanagement.dto.response.ApiResponse;
import com.swp391.rescuemanagement.dto.response.TeamResponse;
import com.swp391.rescuemanagement.service.AuthService;
import com.swp391.rescuemanagement.service.TeamService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;
    private final AuthService authService;

    /**
     * GET /api/teams
     * Tất cả team.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TeamResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(teamService.getAll()));
    }

    /**
     * GET /api/teams/available
     * BƯỚC 3: Danh sách team đang RẢNH.
     * Điều kiện: status='active' VÀ không có TaskGroup assigned/in_progress.
     */
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<TeamResponse>>> available() {
        return ResponseEntity.ok(
                ApiResponse.ok(teamService.getAvailableTeams(), "Danh sách team đang rảnh"));
    }

    /**
     * GET /api/teams/busy
     * BƯỚC 3: Danh sách team đang BẬN.
     */
    @GetMapping("/busy")
    public ResponseEntity<ApiResponse<List<TeamResponse>>> busy() {
        return ResponseEntity.ok(
                ApiResponse.ok(teamService.getBusyTeams(), "Danh sách team đang bận"));
    }

    /**
     * GET /api/teams/{id}/availability
     * Kiểm tra trạng thái rảnh/bận của 1 team.
     * Response: { "teamId": 1, "availability": "available" | "busy" | "inactive" }
     */
    @GetMapping("/{id}/availability")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkAvailability(
            @PathVariable Long id) {

        String avail = teamService.getTeamAvailability(id);
        return ResponseEntity.ok(ApiResponse.ok(
                Map.of("teamId", id, "availability", avail)));
    }

    /**
     * PUT /api/teams/{id}/status
     * Admin cập nhật status team.
     * Body: { "status": "active" | "inactive" }
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TeamResponse>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpServletRequest httpRequest) {

        authService.getCurrentUser(httpRequest); // phải đăng nhập
        String newStatus = body.getOrDefault("status", "");
        return ResponseEntity.ok(
                ApiResponse.ok(teamService.updateStatus(id, newStatus), "Cập nhật team thành công"));
    }
}
