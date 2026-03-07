package com.floodrescue.module.team.service;

import com.floodrescue.module.rescue.dto.response.RescueRequestResponse;
import com.floodrescue.module.rescue.entity.RescueRequestEntity;
import com.floodrescue.module.rescue.mapper.RescueRequestMapper;
import com.floodrescue.module.rescue.repository.RescueRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final RescueRequestRepository rescueRequestRepository;
    private final RescueRequestMapper rescueRequestMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<RescueRequestResponse> getAssignedRescueRequests(Long teamId, Pageable pageable) {
        Page<RescueRequestEntity> entities = rescueRequestRepository.findActiveRequestsByTeamId(teamId, pageable);
        return entities.map(rescueRequestMapper::toResponse);
    }
}
