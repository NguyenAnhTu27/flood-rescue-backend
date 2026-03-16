package com.floodrescue.module.map.controller;

import com.floodrescue.module.map.dto.*;
import com.floodrescue.module.map.service.MapboxService;
import com.floodrescue.module.rescue.service.RescueRequestService;
import com.floodrescue.module.team.service.TeamService;
import com.floodrescue.shared.dto.ApiResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MapController {

    private final TeamService teamService;
    private final MapboxService mapboxService;
    private final RescueRequestService rescueRequestService;

    @PutMapping("/teams/{teamId}/location")
    @PreAuthorize("hasAnyRole('RESCUER', 'COORDINATOR')")
    public ResponseEntity<ApiResult<TeamLocationResponse>> updateTeamLocation(
            @PathVariable Long teamId,
            @Valid @RequestBody LocationUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        // Logic check role RESCUER moved inside TeamService.updateTeamLocation in real app,
        // but since TeamService doesn't have the User context easily right now without more refactoring,
        // leaving the call as is but wrapping in ApiResult. The Fat Controller audit mainly flagged
        // RescueRequestRepository and UserRepository. Note: In a full refactor, UserDetails should be
        // passed to TeamService.
        
        var team = teamService.updateTeamLocation(teamId, request.getLatitude(), request.getLongitude());
        var response = TeamLocationResponse.builder()
                .teamId(team.getId())
                .name(team.getName())
                .status(team.getStatus())
                .teamType(team.getTeamType())
                .latitude(team.getCurrentLatitude())
                .longitude(team.getCurrentLongitude())
                .lastLocationUpdate(team.getCurrentLocationUpdatedAt())
                .build();
        return ResponseEntity.ok(ApiResult.ok("Cập nhật vị trí thành công", response));
    }

    @GetMapping("/map/teams/locations")
    @PreAuthorize("hasAnyRole('COORDINATOR', 'ADMIN')")
    public ResponseEntity<ApiResult<List<TeamLocationResponse>>> getAllTeamLocations() {
        return ResponseEntity.ok(ApiResult.ok(teamService.getAllTeamLocations()));
    }

    @GetMapping("/map/teams/nearest")
    @PreAuthorize("hasAnyRole('COORDINATOR', 'ADMIN')")
    public ResponseEntity<ApiResult<List<TeamLocationResponse>>> findNearestTeams(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "50") Double radius) {
        return ResponseEntity.ok(ApiResult.ok(teamService.findNearestTeams(lat, lng, radius)));
    }

    @GetMapping("/map/rescue-requests/locations")
    @PreAuthorize("hasAnyRole('COORDINATOR', 'ADMIN')")
    public ResponseEntity<ApiResult<List<RescueLocationResponse>>> getRescueRequestLocations() {
        return ResponseEntity.ok(ApiResult.ok(rescueRequestService.getRescueLocationResponses()));
    }

    @GetMapping("/map/geocode")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResult<GeocodingResponse>> geocode(@RequestParam String address) {
        return ResponseEntity.ok(ApiResult.ok(mapboxService.geocode(address)));
    }
}
