package com.floodrescue.module.relief.controller;

import com.floodrescue.module.relief.dto.request.ReliefApproveDispatchRequest;
import com.floodrescue.module.relief.dto.request.ReliefRequestCreateRequest;
import com.floodrescue.module.relief.dto.request.ReliefRequestRejectRequest;
import com.floodrescue.module.relief.dto.request.ReliefRescuerStatusUpdateRequest;
import com.floodrescue.module.relief.dto.response.ManagerReliefDashboardResponse;
import com.floodrescue.module.relief.dto.response.ManagerReliefDispatchDashboardResponse;
import com.floodrescue.module.relief.dto.response.ReliefRequestResponse;
import com.floodrescue.module.relief.service.ManagerReliefDashboardService;
import com.floodrescue.module.relief.service.ManagerReliefDispatchDashboardService;
import com.floodrescue.module.relief.service.ReliefRequestService;
import com.floodrescue.module.user.service.UserService;
import com.floodrescue.shared.dto.ApiResult;
import com.floodrescue.shared.dto.PagedData;
import com.floodrescue.shared.enums.InventoryDocumentStatus;
import com.floodrescue.shared.util.CodeGenerator;
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

import java.util.Map;

@RestController
@RequestMapping({"/api/manager/relief", "/api/relief"})
@RequiredArgsConstructor
public class ReliefRequestController {

    private final ManagerReliefDashboardService dashboardService;
    private final ManagerReliefDispatchDashboardService dispatchDashboardService;
    private final ReliefRequestService reliefRequestService;
    private final UserService userService;

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
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResult<ManagerReliefDashboardResponse>> getManagerDashboard() {
        return ResponseEntity.ok(ApiResult.ok(dashboardService.getDashboard()));
    }

    @GetMapping("/dispatch-dashboard")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResult<ManagerReliefDispatchDashboardResponse>> getManagerDispatchDashboard() {
        return ResponseEntity.ok(ApiResult.ok(dispatchDashboardService.getDashboard()));
    }

    /**
     * Tạo yêu cầu cứu trợ mới.
     * FE có thể gọi: POST /api/relief/requests
     */
    @PostMapping("/requests")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','CITIZEN')")
    public ResponseEntity<ApiResult<ReliefRequestResponse>> createReliefRequest(
            @Valid @RequestBody ReliefRequestCreateRequest request,
            Authentication authentication
    ) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(ApiResult.ok("Tạo yêu cầu cứu trợ thành công", reliefRequestService.createReliefRequest(userId, request)));
    }

    /**
     * Lấy chi tiết yêu cầu cứu trợ.
     */
    @GetMapping("/requests/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','CITIZEN','RESCUER')")
    public ResponseEntity<ApiResult<ReliefRequestResponse>> getReliefRequest(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResult.ok(reliefRequestService.getReliefRequest(id)));
    }

    /**
     * Danh sách yêu cầu cứu trợ (lọc theo trạng thái nếu cần).
     */
    @GetMapping("/requests")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResult<PagedData<ReliefRequestResponse>>> listReliefRequests(
            @RequestParam(required = false) InventoryDocumentStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResult.ok(PagedData.from(reliefRequestService.listReliefRequests(status, pageable))));
    }

    /**
     * Duyệt yêu cầu cứu trợ (đơn giản).
     */
    @PutMapping("/requests/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResult<ReliefRequestResponse>> approveReliefRequest(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(ApiResult.ok("Duyệt yêu cầu thành công", reliefRequestService.approveReliefRequest(id, userId)));
    }

    @GetMapping("/requests/generate-code")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','CITIZEN')")
    public ResponseEntity<ApiResult<Map<String, String>>> generateReliefRequestCode() {
        return ResponseEntity.ok(ApiResult.ok(Map.of("code", CodeGenerator.generateInventoryReceiptCode())));
    }

    @GetMapping("/citizen/requests")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<ApiResult<PagedData<ReliefRequestResponse>>> listMyReliefRequests(
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication
    ) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(ApiResult.ok(PagedData.from(reliefRequestService.listMyReliefRequests(userId, pageable))));
    }

    @PutMapping("/citizen/requests/{id}")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<ApiResult<ReliefRequestResponse>> updateCitizenReliefRequest(
            @PathVariable Long id,
            @Valid @RequestBody ReliefRequestCreateRequest request,
            Authentication authentication
    ) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(ApiResult.ok("Cập nhật yêu cầu thành công", reliefRequestService.updateCitizenReliefRequest(id, userId, request)));
    }

    @DeleteMapping("/citizen/requests/{id}")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<ApiResult<Void>> cancelCitizenReliefRequest(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            Authentication authentication
    ) {
        Long userId = getCurrentUserId(authentication);
        reliefRequestService.cancelCitizenReliefRequest(id, userId, reason);
        return ResponseEntity.ok(ApiResult.ok("Yêu cầu cứu trợ đã được hủy"));
    }

    @PutMapping("/requests/{id}/approve-dispatch")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResult<ReliefRequestResponse>> approveAndDispatch(
            @PathVariable Long id,
            @Valid @RequestBody ReliefApproveDispatchRequest request,
            Authentication authentication
    ) {
        Long managerId = getCurrentUserId(authentication);
        return ResponseEntity.ok(ApiResult.ok("Duyệt và điều phối thành công", reliefRequestService.approveAndDispatch(id, managerId, request.getAssignedTeamId(), request.getNote())));
    }

    /**
     * Từ chối yêu cầu cứu trợ.
     */
    @PutMapping("/requests/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResult<ReliefRequestResponse>> rejectReliefRequest(
            @PathVariable Long id,
            @Valid @RequestBody ReliefRequestRejectRequest request,
            Authentication authentication
    ) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(ApiResult.ok("Đã từ chối yêu cầu", reliefRequestService.rejectReliefRequest(id, userId, request.getReason())));
    }

    @GetMapping("/rescuer/requests")
    @PreAuthorize("hasRole('RESCUER')")
    public ResponseEntity<ApiResult<PagedData<ReliefRequestResponse>>> listRescuerReliefRequests(
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication
    ) {
        Long teamId = getCurrentUserTeamId(authentication);
        if (teamId == null) {
            return ResponseEntity.ok(ApiResult.ok(PagedData.from(Page.empty(pageable))));
        }
        return ResponseEntity.ok(ApiResult.ok(PagedData.from(reliefRequestService.listRescuerAssignedReliefRequests(teamId, pageable))));
    }

    @PutMapping("/rescuer/requests/{id}/delivery-status")
    @PreAuthorize("hasRole('RESCUER')")
    public ResponseEntity<ApiResult<ReliefRequestResponse>> updateRescuerDeliveryStatus(
            @PathVariable Long id,
            @Valid @RequestBody ReliefRescuerStatusUpdateRequest request,
            Authentication authentication
    ) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(ApiResult.ok("Cập nhật trạng thái giao hàng thành công", reliefRequestService.updateRescuerDeliveryStatus(userId, id, request.getStatus(), request.getNote())));
    }

    private Long getCurrentUserTeamId(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return userService.getUserTeamId(userId);
    }
}
