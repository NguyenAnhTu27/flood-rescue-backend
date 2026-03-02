package com.floodrescue.module.team.controller;

import com.floodrescue.module.rescue.dto.response.RescueRequestResponse;
import com.floodrescue.module.team.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @GetMapping("/{teamId}/requests")
    public ResponseEntity<Page<RescueRequestResponse>> getTeamRequests(
            @PathVariable Long teamId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(teamService.getAssignedRescueRequests(teamId, pageable));
    }
}
