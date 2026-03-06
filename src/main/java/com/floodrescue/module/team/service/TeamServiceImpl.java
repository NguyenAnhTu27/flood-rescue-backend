package com.floodrescue.module.team.service;

import com.floodrescue.module.map.dto.LocationUpdateRequest;
import com.floodrescue.module.map.dto.TeamLocationResponse;
import com.floodrescue.module.map.service.MapboxService;
import com.floodrescue.module.rescue.dto.response.RescueRequestResponse;
import com.floodrescue.module.rescue.entity.RescueRequestEntity;
import com.floodrescue.module.rescue.mapper.RescueRequestMapper;
import com.floodrescue.module.rescue.repository.RescueRequestRepository;
import com.floodrescue.module.team.entity.TeamEntity;
import com.floodrescue.module.team.repository.TeamRepository;
import com.floodrescue.shared.enums.TeamStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final RescueRequestRepository rescueRequestRepository;
    private final RescueRequestMapper rescueRequestMapper;
    private final TeamRepository teamRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<RescueRequestResponse> getAssignedRescueRequests(Long teamId, Pageable pageable) {
        Page<RescueRequestEntity> entities = rescueRequestRepository.findActiveRequestsByTeamId(teamId, pageable);
        return entities.map(rescueRequestMapper::toResponse);
    }

    @Override
    @Transactional
    public TeamLocationResponse updateTeamLocation(Long teamId, LocationUpdateRequest request) {
        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found with id: " + teamId));

        team.setLatitude(request.getLatitude());
        team.setLongitude(request.getLongitude());
        team.setLastLocationUpdate(LocalDateTime.now());
        teamRepository.save(team);

        return toLocationResponse(team, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamLocationResponse> getAllTeamLocations() {
        List<TeamEntity> teams = teamRepository.findByStatusAndLatitudeIsNotNull(TeamStatus.ACTIVE);
        return teams.stream()
                .map(t -> toLocationResponse(t, null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamLocationResponse> findNearestTeams(double latitude, double longitude, double radiusKm) {
        List<TeamEntity> activeTeams = teamRepository.findByStatusAndLatitudeIsNotNull(TeamStatus.ACTIVE);

        return activeTeams.stream()
                .map(team -> {
                    double distance = MapboxService.haversineDistance(
                            latitude, longitude,
                            team.getLatitude(), team.getLongitude());
                    return toLocationResponse(team, distance);
                })
                .filter(response -> response.getDistanceKm() <= radiusKm)
                .sorted(Comparator.comparingDouble(TeamLocationResponse::getDistanceKm))
                .collect(Collectors.toList());
    }

    private TeamLocationResponse toLocationResponse(TeamEntity team, Double distanceKm) {
        return TeamLocationResponse.builder()
                .teamId(team.getId())
                .code(team.getCode())
                .name(team.getName())
                .status(team.getStatus().name())
                .latitude(team.getLatitude())
                .longitude(team.getLongitude())
                .lastLocationUpdate(team.getLastLocationUpdate())
                .distanceKm(distanceKm)
                .build();
    }
}
