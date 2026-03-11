package com.floodrescue.module.team.service;

import com.floodrescue.module.map.dto.TeamLocationResponse;
import com.floodrescue.module.map.service.MapboxService;
import com.floodrescue.module.rescue.dto.response.RescueRequestResponse;
import com.floodrescue.module.rescue.mapper.RescueRequestMapper;
import com.floodrescue.module.rescue.repository.RescueRequestRepository;
import com.floodrescue.module.team.dto.request.CreateTeamRequest;
import com.floodrescue.module.team.entity.TeamEntity;
import com.floodrescue.module.team.repository.TeamRepository;
import com.floodrescue.shared.exception.BusinessException;
import com.floodrescue.shared.exception.NotFoundException;
import com.floodrescue.shared.util.CodeGenerator;
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

    private final TeamRepository teamRepository;
    private final RescueRequestRepository rescueRequestRepository;
    private final RescueRequestMapper rescueRequestMapper;
    private final MapboxService mapboxService;

    @Override
    @Transactional
    public TeamEntity createTeam(CreateTeamRequest request) {
        String name = request.getName().trim();
        // Không cho trùng tên đội (Đội cứu hộ số 1, 2, 3...)
        if (teamRepository.existsByName(name)) {
            throw new BusinessException("Tên đội cứu hộ đã tồn tại");
        }

        // Generate unique code
        String code;
        int attempts = 0;
        do {
            code = CodeGenerator.generateTeamCode();
            attempts++;
            if (attempts > 10) {
                throw new BusinessException("Không thể tạo mã đội cứu hộ duy nhất");
            }
        } while (teamRepository.existsByCode(code));

        TeamEntity team = TeamEntity.builder()
                .code(code)
                .name(name)
                .description(request.getDescription())
                .build();
        return teamRepository.save(team);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamEntity> getAllTeams() {
        return teamRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public TeamEntity getTeamById(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Đội cứu hộ không tồn tại"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RescueRequestResponse> getAssignedRescueRequests(Long teamId, Pageable pageable) {
        return rescueRequestRepository.findActiveRequestsByTeamId(teamId, pageable)
                .map(rescueRequestMapper::toResponse);
    }

    @Override
    @Transactional
    public TeamEntity updateTeamLocation(Long teamId, Double latitude, Double longitude) {
        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(() -> new NotFoundException("Đội cứu hộ không tồn tại"));
        team.setLatitude(latitude);
        team.setLongitude(longitude);
        team.setLastLocationUpdate(LocalDateTime.now());
        return teamRepository.save(team);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamLocationResponse> getAllTeamLocations() {
        return teamRepository.findByLatitudeIsNotNull().stream()
                .map(team -> TeamLocationResponse.builder()
                        .teamId(team.getId())
                        .name(team.getName())
                        .status(team.getStatus())
                        .teamType(team.getTeamType())
                        .latitude(team.getLatitude())
                        .longitude(team.getLongitude())
                        .lastLocationUpdate(team.getLastLocationUpdate())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamLocationResponse> findNearestTeams(Double latitude, Double longitude, Double radiusKm) {
        return teamRepository.findByLatitudeIsNotNull().stream()
                .map(team -> {
                    double distance = mapboxService.calculateDistance(
                            latitude, longitude, team.getLatitude(), team.getLongitude());
                    return TeamLocationResponse.builder()
                            .teamId(team.getId())
                            .name(team.getName())
                            .status(team.getStatus())
                            .teamType(team.getTeamType())
                            .latitude(team.getLatitude())
                            .longitude(team.getLongitude())
                            .distanceKm(Math.round(distance * 100.0) / 100.0)
                            .lastLocationUpdate(team.getLastLocationUpdate())
                            .build();
                })
                .filter(t -> t.getDistanceKm() <= radiusKm)
                .sorted(Comparator.comparingDouble(TeamLocationResponse::getDistanceKm))
                .collect(Collectors.toList());
    }
}   