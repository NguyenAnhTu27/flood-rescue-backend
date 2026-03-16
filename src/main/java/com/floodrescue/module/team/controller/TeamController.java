package com.floodrescue.module.team.controller;

import com.floodrescue.module.team.dto.request.CreateTeamRequest;
import com.floodrescue.module.team.dto.response.TeamMemberResponse;
import com.floodrescue.module.team.dto.response.TeamResponse;
import com.floodrescue.module.team.service.TeamService;
import com.floodrescue.shared.dto.ApiResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    /**
     * Tạo đội cứu hộ mới (dành cho Admin/Manager).
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResult<TeamResponse>> createTeam(@Valid @RequestBody CreateTeamRequest request) {
        TeamResponse team = teamService.createTeam(request);
        return ResponseEntity.ok(ApiResult.ok("Tạo đội cứu hộ thành công", team));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResult<TeamResponse>> updateTeam(@PathVariable Long id, @Valid @RequestBody CreateTeamRequest request) {
        return ResponseEntity.ok(ApiResult.ok("Cập nhật đội cứu hộ thành công", teamService.updateTeam(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResult<Void>> deleteTeam(@PathVariable Long id) {
        teamService.deleteTeam(id);
        return ResponseEntity.ok(ApiResult.ok("Xóa đội cứu hộ thành công"));
    }

    /**
     * Lấy danh sách tất cả đội cứu hộ.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','COORDINATOR')")
    public ResponseEntity<ApiResult<List<TeamResponse>>> getTeams() {
        return ResponseEntity.ok(ApiResult.ok(teamService.getAllTeams()));
    }

    /**
     * Lấy chi tiết 1 đội cứu hộ theo ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','COORDINATOR')")
    public ResponseEntity<ApiResult<TeamResponse>> getTeam(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResult.ok(teamService.getTeamById(id)));
    }

    @GetMapping("/member-candidates")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResult<List<TeamMemberResponse>>> getMemberCandidates() {
        return ResponseEntity.ok(ApiResult.ok(teamService.getRescuerCandidates()));
    }
}
