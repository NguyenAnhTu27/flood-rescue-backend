package com.floodrescue.module.team.service;

import com.floodrescue.module.map.dto.TeamLocationResponse;
import com.floodrescue.module.rescue.dto.response.RescueRequestResponse;
import com.floodrescue.module.team.dto.request.CreateTeamRequest;
import com.floodrescue.module.team.entity.TeamEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TeamService {

    TeamEntity createTeam(CreateTeamRequest request);

    List<TeamEntity> getAllTeams();

    TeamEntity getTeamById(Long id);

    Page<RescueRequestResponse> getAssignedRescueRequests(Long teamId, Pageable pageable);

    TeamEntity updateTeamLocation(Long teamId, Double latitude, Double longitude);

    List<TeamLocationResponse> getAllTeamLocations();

    List<TeamLocationResponse> findNearestTeams(Double latitude, Double longitude, Double radiusKm);
}