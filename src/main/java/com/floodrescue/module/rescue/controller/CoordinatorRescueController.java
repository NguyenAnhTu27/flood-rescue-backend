package com.floodrescue.module.rescue.controller;

import com.floodrescue.module.rescue.dto.request.*;
import com.floodrescue.module.rescue.dto.response.CoordinatorDashboardResponse;
import com.floodrescue.module.rescue.dto.response.BlockedCitizenResponse;
import com.floodrescue.module.rescue.dto.response.RescueRequestResponse;
import com.floodrescue.module.rescue.dto.response.TaskGroupResponse;
import com.floodrescue.module.rescue.service.RescueRequestService;
import com.floodrescue.module.rescue.service.TaskGroupService;
import com.floodrescue.module.rescue.service.AssignmentService;
import com.floodrescue.module.rescue.service.CoordinatorDashboardService;
import com.floodrescue.shared.dto.ApiResult;
import com.floodrescue.shared.dto.PagedData;
import com.floodrescue.shared.enums.RescuePriority;
import com.floodrescue.shared.enums.RescueRequestStatus;
import com.floodrescue.shared.enums.TaskGroupStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rescue/coordinator")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('COORDINATOR','ADMIN')")
public class CoordinatorRescueController {

    private final RescueRequestService rescueRequestService;
    private final TaskGroupService taskGroupService;
    private final AssignmentService assignmentService;
    private final CoordinatorDashboardService coordinatorDashboardService;

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return Long.parseLong(userDetails.getUsername());
    }

    @GetMapping("/requests")
    public ResponseEntity<ApiResult<PagedData<RescueRequestResponse>>> getRescueRequests(
            @RequestParam(required = false) RescueRequestStatus status,
            @RequestParam(required = false) RescuePriority priority,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<RescueRequestResponse> response;
        if (status == null && priority == null && keyword == null && pageable.getSort().isUnsorted()) {
            response = rescueRequestService.getPendingRescueRequests(pageable);
        } else {
            response = rescueRequestService.searchRescueRequests(status, priority, keyword, pageable);
        }
        return ResponseEntity.ok(ApiResult.ok(PagedData.from(response)));
    }

    @GetMapping("/requests/{id}")
    public ResponseEntity<ApiResult<RescueRequestResponse>> getRescueRequestById(@PathVariable Long id) {
        RescueRequestResponse response = rescueRequestService.getRescueRequestById(id);
        return ResponseEntity.ok(ApiResult.ok(response));
    }

    @GetMapping("/requests/code/{code}")
    public ResponseEntity<ApiResult<RescueRequestResponse>> getRescueRequestByCode(@PathVariable String code) {
        RescueRequestResponse response = rescueRequestService.getRescueRequestByCode(code);
        return ResponseEntity.ok(ApiResult.ok(response));
    }

    @PostMapping("/requests/{id}/verify")
    public ResponseEntity<ApiResult<RescueRequestResponse>> verifyRescueRequest(
            @PathVariable Long id,
            @Valid @RequestBody VerifyRequest request,
            Authentication authentication) {
        Long coordinatorId = getCurrentUserId(authentication);
        RescueRequestResponse response = rescueRequestService.verifyRescueRequest(id, coordinatorId, request);
        return ResponseEntity.ok(ApiResult.ok("Xác minh thành công", response));
    }

    @PutMapping("/requests/{id}/priority")
    public ResponseEntity<ApiResult<RescueRequestResponse>> prioritizeRescueRequest(
            @PathVariable Long id,
            @Valid @RequestBody PrioritizeRequest request,
            Authentication authentication) {
        Long coordinatorId = getCurrentUserId(authentication);
        RescueRequestResponse response = rescueRequestService.prioritizeRescueRequest(id, coordinatorId, request);
        return ResponseEntity.ok(ApiResult.ok("Cập nhật mức độ ưu tiên thành công", response));
    }

    @PostMapping("/requests/{id}/duplicate")
    public ResponseEntity<ApiResult<RescueRequestResponse>> markAsDuplicate(
            @PathVariable Long id,
            @Valid @RequestBody MarkDuplicateRequest request,
            Authentication authentication) {
        Long coordinatorId = getCurrentUserId(authentication);
        RescueRequestResponse response = rescueRequestService.markAsDuplicate(id, coordinatorId, request);
        return ResponseEntity.ok(ApiResult.ok("Đánh dấu trùng lặp thành công", response));
    }

    @PutMapping("/requests/{id}/status")
    public ResponseEntity<ApiResult<RescueRequestResponse>> changeStatus(
            @PathVariable Long id,
            @RequestParam RescueRequestStatus status,
            @RequestParam(required = false) String note,
            Authentication authentication) {
        Long coordinatorId = getCurrentUserId(authentication);
        RescueRequestResponse response = rescueRequestService.changeStatus(id, coordinatorId, status, note);
        return ResponseEntity.ok(ApiResult.ok("Cập nhật trạng thái thành công", response));
    }

    @PostMapping("/requests/{id}/notes")
    public ResponseEntity<ApiResult<RescueRequestResponse>> addNote(
            @PathVariable Long id,
            @Valid @RequestBody AddNoteRequest request,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        RescueRequestResponse response = rescueRequestService.addNote(id, userId, request);
        return ResponseEntity.ok(ApiResult.ok("Thêm ghi chú thành công", response));
    }

    @PostMapping("/requests/{id}/citizen-block")
    public ResponseEntity<ApiResult<Void>> blockCitizenFromRequest(
            @PathVariable Long id,
            @Valid @RequestBody BlockCitizenRequest request,
            Authentication authentication
    ) {
        Long coordinatorId = getCurrentUserId(authentication);
        rescueRequestService.setCitizenRequestBlockByRequest(
                id,
                coordinatorId,
                Boolean.TRUE.equals(request.getBlocked()),
                request.getReason()
        );
        return ResponseEntity.ok(ApiResult.ok("Đã chặn người dùng"));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResult<CoordinatorDashboardResponse>> getDashboard(Authentication authentication) {
        Long coordinatorId = getCurrentUserId(authentication);
        return ResponseEntity.ok(ApiResult.ok(coordinatorDashboardService.getDashboard(coordinatorId)));
    }

    @GetMapping("/citizens/blocked")
    public ResponseEntity<ApiResult<List<BlockedCitizenResponse>>> getBlockedCitizens() {
        return ResponseEntity.ok(ApiResult.ok(rescueRequestService.getBlockedCitizens()));
    }

    @PostMapping("/citizens/{citizenId}/unblock")
    public ResponseEntity<ApiResult<Void>> unblockCitizen(
            @PathVariable Long citizenId,
            @Valid @RequestBody(required = false) UnblockCitizenRequest request,
            Authentication authentication
    ) {
        Long coordinatorId = getCurrentUserId(authentication);
        rescueRequestService.unblockCitizen(
                citizenId,
                coordinatorId,
                request == null ? null : request.getReason()
        );
        return ResponseEntity.ok(ApiResult.ok("Đã mở khóa người dùng"));
    }

    // ===== Task Group APIs =====

    @PostMapping("/task-groups")
    public ResponseEntity<ApiResult<TaskGroupResponse>> createTaskGroup(
            @Valid @RequestBody CreateTaskGroupRequest request,
            Authentication authentication
    ) {
        Long coordinatorId = getCurrentUserId(authentication);
        return ResponseEntity.ok(ApiResult.ok("Tạo nhóm công việc thành công", taskGroupService.createTaskGroup(request, coordinatorId)));
    }

    @GetMapping("/task-groups")
    public ResponseEntity<ApiResult<PagedData<TaskGroupResponse>>> getTaskGroups(
            @RequestParam(required = false) TaskGroupStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResult.ok(PagedData.from(taskGroupService.getTaskGroups(status, pageable))));
    }

    @GetMapping("/task-groups/{id}")
    public ResponseEntity<ApiResult<TaskGroupResponse>> getTaskGroupById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResult.ok(taskGroupService.getTaskGroup(id)));
    }

    @PutMapping("/task-groups/{id}/status")
    public ResponseEntity<ApiResult<TaskGroupResponse>> changeTaskGroupStatus(
            @PathVariable Long id,
            @RequestParam TaskGroupStatus status,
            @RequestParam(required = false) String note,
            Authentication authentication
    ) {
        Long coordinatorId = getCurrentUserId(authentication);
        return ResponseEntity.ok(ApiResult.ok("Cập nhật trạng thái nhóm công việc thành công", taskGroupService.changeStatus(id, status, note, coordinatorId)));
    }

    @PostMapping("/task-groups/assign")
    public ResponseEntity<ApiResult<TaskGroupResponse>> assignTaskGroup(
            @Valid @RequestBody AssignTaskGroupRequest request,
            Authentication authentication
    ) {
        Long coordinatorId = getCurrentUserId(authentication);
        return ResponseEntity.ok(ApiResult.ok("Phân công nhóm công việc thành công", assignmentService.assignTaskGroup(request, coordinatorId)));
    }
}
