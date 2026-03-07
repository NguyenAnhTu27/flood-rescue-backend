package com.floodrescue.module.team.controller;

import com.floodrescue.module.team.dto.request.CreateTeamRequest;
import com.floodrescue.module.team.entity.TeamEntity;
import com.floodrescue.module.team.service.TeamService;
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
    public ResponseEntity<TeamEntity> createTeam(@Valid @RequestBody CreateTeamRequest request) {
        TeamEntity team = teamService.createTeam(request);
        return ResponseEntity.ok(team);
    }

    /**
     * Lấy danh sách tất cả đội cứu hộ.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','COORDINATOR')")
    public ResponseEntity<List<TeamEntity>> getTeams() {
        return ResponseEntity.ok(teamService.getAllTeams());
    }

    /**
     * Lấy chi tiết 1 đội cứu hộ theo ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','COORDINATOR')")
    public ResponseEntity<TeamEntity> getTeam(@PathVariable Long id) {
        return ResponseEntity.ok(teamService.getTeamById(id));
    }
}