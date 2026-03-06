package com.floodrescue.module.team.service;

import com.floodrescue.module.map.dto.LocationUpdateRequest;
import com.floodrescue.module.map.dto.TeamLocationResponse;
import com.floodrescue.module.rescue.dto.response.RescueRequestResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TeamService {
    Page<RescueRequestResponse> getAssignedRescueRequests(Long teamId, Pageable pageable);

    TeamLocationResponse updateTeamLocation(Long teamId, LocationUpdateRequest request);

    List<TeamLocationResponse> getAllTeamLocations();

    List<TeamLocationResponse> findNearestTeams(double latitude, double longitude, double radiusKm);
}
