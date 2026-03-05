package com.floodrescue.module.relief.controller;

import com.floodrescue.module.relief.dto.request.ReliefRequestCreateRequest;
import com.floodrescue.module.relief.dto.response.ManagerReliefDashboardResponse;
import com.floodrescue.module.relief.dto.response.ReliefRequestResponse;
import com.floodrescue.module.relief.service.ManagerReliefDashboardService;
import com.floodrescue.module.relief.service.ReliefRequestService;
import com.floodrescue.shared.enums.InventoryDocumentStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/manager/relief", "/api/relief"})
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class ReliefRequestController {

    private final ManagerReliefDashboardService dashboardService;
    private final ReliefRequestService reliefRequestService;

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return Long.parseLong(userDetails.getUsername());
    }

    /**
     * Dashboard cho màn hình Manager cứu trợ.
     * FE đang gọi: GET /api/manager/relief/dashboard hoặc /api/relief/dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ManagerReliefDashboardResponse> getManagerDashboard() {
        return ResponseEntity.ok(dashboardService.getDashboard());
    }

    /**
     * Tạo yêu cầu cứu trợ mới.
     * FE có thể gọi: POST /api/relief/requests
     */
    @PostMapping("/requests")
    public ResponseEntity<ReliefRequestResponse> createReliefRequest(
            @Valid @RequestBody ReliefRequestCreateRequest request,
            Authentication authentication
    ) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(reliefRequestService.createReliefRequest(userId, request));
    }

    /**
     * Lấy chi tiết yêu cầu cứu trợ.
     */
    @GetMapping("/requests/{id}")
    public ResponseEntity<ReliefRequestResponse> getReliefRequest(@PathVariable Long id) {
        return ResponseEntity.ok(reliefRequestService.getReliefRequest(id));
    }

    /**
     * Danh sách yêu cầu cứu trợ (lọc theo trạng thái nếu cần).
     */
    @GetMapping("/requests")
    public ResponseEntity<Page<ReliefRequestResponse>> listReliefRequests(
            @RequestParam(required = false) InventoryDocumentStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(reliefRequestService.listReliefRequests(status, pageable));
    }
}
