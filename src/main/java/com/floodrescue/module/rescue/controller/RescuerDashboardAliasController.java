package com.floodrescue.module.rescue.controller;

import com.floodrescue.module.rescue.dto.response.RescuerDashboardResponse;
import com.floodrescue.module.rescue.service.RescuerTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rescuer")
@RequiredArgsConstructor
@PreAuthorize("hasRole('RESCUER')")
public class RescuerDashboardAliasController {

    private final RescuerTaskService rescuerTaskService;

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return Long.parseLong(userDetails.getUsername());
    }

    @GetMapping("/dashboard")
    public ResponseEntity<RescuerDashboardResponse> getDashboard(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(rescuerTaskService.getDashboard(userId));
    }
}
