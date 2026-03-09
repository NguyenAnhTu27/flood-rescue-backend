package com.floodrescue.module.rescue.controller;

import com.floodrescue.module.rescue.dto.response.CitizenDashboardResponse;
import com.floodrescue.module.rescue.dto.response.RescueRequestResponse;
import com.floodrescue.module.rescue.repository.RescueRequestRepository;
import com.floodrescue.module.rescue.service.RescueRequestService;
import com.floodrescue.shared.enums.RescueRequestStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/citizen")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CITIZEN')")
public class CitizenDashboardController {

    private final RescueRequestService rescueRequestService;
    private final RescueRequestRepository rescueRequestRepository;

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return Long.parseLong(userDetails.getUsername());
    }

    @GetMapping("/dashboard")
    public ResponseEntity<CitizenDashboardResponse> getDashboard(Authentication authentication) {
        Long citizenId = getCurrentUserId(authentication);

        Page<RescueRequestResponse> page = rescueRequestService.getRescueRequestsByCitizen(
                citizenId,
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        CitizenDashboardResponse response = CitizenDashboardResponse.builder()
                .totalRequests(rescueRequestRepository.countByCitizenId(citizenId))
                .pendingRequests(rescueRequestRepository.countByCitizenIdAndStatus(citizenId, RescueRequestStatus.PENDING))
                .inProgressRequests(rescueRequestRepository.countByCitizenIdAndStatus(citizenId, RescueRequestStatus.IN_PROGRESS))
                .completedRequests(rescueRequestRepository.countByCitizenIdAndStatus(citizenId, RescueRequestStatus.COMPLETED))
                .cancelledRequests(rescueRequestRepository.countByCitizenIdAndStatus(citizenId, RescueRequestStatus.CANCELLED))
                .duplicateRequests(rescueRequestRepository.countByCitizenIdAndStatus(citizenId, RescueRequestStatus.DUPLICATE))
                .latestRequest(page.hasContent() ? page.getContent().get(0) : null)
                .recentRequests(page.getContent())
                .build();

        return ResponseEntity.ok(response);
    }
}
