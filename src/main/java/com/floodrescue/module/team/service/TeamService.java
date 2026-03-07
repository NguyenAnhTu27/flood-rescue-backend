package com.floodrescue.module.team.service;

import com.floodrescue.module.rescue.dto.response.RescueRequestResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TeamService {
    Page<RescueRequestResponse> getAssignedRescueRequests(Long teamId, Pageable pageable);
}
