package com.floodrescue.module.rescue.controller;

import com.floodrescue.module.rescue.dto.request.AddNoteRequest;
import com.floodrescue.module.rescue.dto.request.EscalateTaskGroupRequest;
import com.floodrescue.module.rescue.dto.request.UpdateTeamLocationRequest;
import com.floodrescue.module.rescue.dto.response.RescueRequestResponse;
import com.floodrescue.module.rescue.dto.response.RescuerDashboardResponse;
import com.floodrescue.module.rescue.dto.response.TaskGroupResponse;
import com.floodrescue.module.notification.dto.EmergencyAckResponse;
import com.floodrescue.module.rescue.service.RescueRequestService;
import com.floodrescue.module.rescue.service.RescuerTaskService;
import com.floodrescue.shared.dto.ApiResult;
import com.floodrescue.shared.dto.PagedData;
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
import java.util.Map;

@RestController
@RequestMapping("/api/rescue/rescuer")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('RESCUER','ADMIN')")
public class RescuerTaskController {

    private final RescueRequestService rescueRequestService;
    private final RescuerTaskService rescuerTaskService;

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return Long.parseLong(userDetails.getUsername());
    }

    @GetMapping("/tasks")
    public ResponseEntity<ApiResult<PagedData<RescueRequestResponse>>> getMyTasks(
            @RequestParam(required = false) RescueRequestStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<RescueRequestResponse> response;
        if (status != null) {
            response = rescueRequestService.getRescueRequestsByStatus(status, pageable);
        } else {
            response = rescueRequestService.getRescueRequestsByStatus(RescueRequestStatus.IN_PROGRESS, pageable);
        }
        return ResponseEntity.ok(ApiResult.ok(PagedData.from(response)));
    }

    @GetMapping("/tasks/{id}")
    public ResponseEntity<ApiResult<RescueRequestResponse>> getTaskById(@PathVariable Long id) {
        RescueRequestResponse response = rescueRequestService.getRescueRequestById(id);
        return ResponseEntity.ok(ApiResult.ok(response));
    }

    @PutMapping("/tasks/{id}/status")
    public ResponseEntity<ApiResult<RescueRequestResponse>> updateTaskStatus(
            @PathVariable Long id,
            @RequestParam RescueRequestStatus status,
            @RequestParam(required = false) String note,
            Authentication authentication) {
        Long rescuerId = getCurrentUserId(authentication);
        RescueRequestResponse response = rescueRequestService.changeStatus(id, rescuerId, status, note);
        return ResponseEntity.ok(ApiResult.ok("Cập nhật trạng thái thành công", response));
    }

    @PostMapping("/tasks/{id}/notes")
    public ResponseEntity<ApiResult<RescueRequestResponse>> addNote(
            @PathVariable Long id,
            @Valid @RequestBody AddNoteRequest request,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        RescueRequestResponse response = rescueRequestService.addNote(id, userId, request);
        return ResponseEntity.ok(ApiResult.ok("Thêm ghi chú thành công", response));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResult<RescuerDashboardResponse>> getDashboard(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(ApiResult.ok(rescuerTaskService.getDashboard(userId)));
    }

    @GetMapping("/task-groups")
    public ResponseEntity<ApiResult<PagedData<TaskGroupResponse>>> getMyTaskGroups(
            @RequestParam(required = false) TaskGroupStatus status,
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication
    ) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(ApiResult.ok(PagedData.from(rescuerTaskService.getMyTaskGroups(userId, status, pageable))));
    }

    @GetMapping("/task-groups/{id}")
    public ResponseEntity<ApiResult<TaskGroupResponse>> getMyTaskGroup(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(ApiResult.ok(rescuerTaskService.getMyTaskGroup(userId, id)));
    }

    @PutMapping("/task-groups/{id}/status")
    public ResponseEntity<ApiResult<TaskGroupResponse>> updateMyTaskGroupStatus(
            @PathVariable Long id,
            @RequestParam TaskGroupStatus status,
            @RequestParam(required = false) String note,
            Authentication authentication
    ) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(ApiResult.ok("Cập nhật nhóm công việc thành công", rescuerTaskService.updateMyTaskGroupStatus(userId, id, status, note)));
    }

    @PostMapping("/task-groups/{id}/escalate")
    public ResponseEntity<ApiResult<TaskGroupResponse>> escalateMyTaskGroup(
            @PathVariable Long id,
            @Valid @RequestBody EscalateTaskGroupRequest request,
            Authentication authentication
    ) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(ApiResult.ok("Báo cáo khẩn cấp thành công", rescuerTaskService.escalateMyTaskGroup(userId, id, request)));
    }

    @GetMapping("/task-groups/{id}/emergency-acks")
    public ResponseEntity<ApiResult<List<EmergencyAckResponse>>> getEmergencyAcks(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(ApiResult.ok(rescuerTaskService.getEmergencyAcks(userId, id)));
    }

    @PostMapping("/team-location")
    public ResponseEntity<ApiResult<Void>> updateTeamLocation(
            @Valid @RequestBody UpdateTeamLocationRequest request,
            Authentication authentication
    ) {
        Long userId = getCurrentUserId(authentication);
        rescuerTaskService.updateMyTeamLocation(
                userId,
                request.getLatitude(),
                request.getLongitude(),
                request.getLocationText()
        );
        return ResponseEntity.ok(ApiResult.ok("Đã cập nhật vị trí đội cứu hộ"));
    }

    @PostMapping("/assets/return")
    public ResponseEntity<ApiResult<Map<String, Object>>> returnMyTeamAssets(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        long returnedCount = rescuerTaskService.returnMyTeamAssets(userId);
        return ResponseEntity.ok(ApiResult.ok("Đã trả tài sản về trạng thái sẵn sàng", Map.of("returnedAssetCount", returnedCount)));
    }
}
