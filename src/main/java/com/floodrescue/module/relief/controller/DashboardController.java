package com.floodrescue.module.relief.controller;

import com.floodrescue.module.relief.dto.response.ManagerReliefDashboardResponse;
import com.floodrescue.module.relief.dto.response.ManagerReliefDispatchDashboardResponse;
import com.floodrescue.module.relief.service.ManagerReliefDashboardService;
import com.floodrescue.module.relief.service.ManagerReliefDispatchDashboardService;
import com.floodrescue.shared.dto.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboards")
@RequiredArgsConstructor
public class DashboardController {

    private final ManagerReliefDashboardService managerReliefDashboardService;
    private final ManagerReliefDispatchDashboardService managerReliefDispatchDashboardService;

    /**
     * Manager Relief Dashboard
     * GET /api/dashboards/manager-relief
     */
    @GetMapping("/manager-relief")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResult<ManagerReliefDashboardResponse>> getManagerReliefDashboard() {
        return ResponseEntity.ok(ApiResult.ok(managerReliefDashboardService.getDashboard()));
    }

    /**
     * Manager Relief Dispatch Dashboard
     * GET /api/dashboards/manager-relief-dispatch
     */
    @GetMapping("/manager-relief-dispatch")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResult<ManagerReliefDispatchDashboardResponse>> getManagerReliefDispatchDashboard() {
        return ResponseEntity.ok(ApiResult.ok(managerReliefDispatchDashboardService.getDashboard()));
    }
}
