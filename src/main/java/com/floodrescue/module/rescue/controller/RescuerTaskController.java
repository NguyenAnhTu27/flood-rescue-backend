package com.floodrescue.module.rescue.controller;

import com.floodrescue.module.rescue.dto.response.RescuerDashboardResponse;
import com.floodrescue.module.rescue.dto.response.TaskGroupResponse;
import com.floodrescue.module.rescue.service.RescuerTaskService;
import com.floodrescue.shared.enums.TaskGroupStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/rescue/rescuer", "/api/rescuer"})
@RequiredArgsConstructor
@PreAuthorize("hasRole('RESCUER')")
public class RescuerTaskController {

    private final RescuerTaskService rescuerTaskService;

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return Long.parseLong(userDetails.getUsername());
    }

    /**
     * Dashboard của đội cứu hộ: trả team info + các nhóm nhiệm vụ của đội.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<RescuerDashboardResponse> getDashboard(Authentication authentication) {
        Long rescuerId = getCurrentUserId(authentication);
        return ResponseEntity.ok(rescuerTaskService.getDashboard(rescuerId));
    }

    /**
     * Danh sách nhóm nhiệm vụ của đội (filter theo status nếu cần).
     * Backward compatible: /tasks và /task-groups đều dùng chung.
     */
    @GetMapping({"/tasks", "/task-groups"})
    public ResponseEntity<Page<TaskGroupResponse>> getMyTaskGroups(
            @RequestParam(required = false) TaskGroupStatus status,
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication
    ) {
        Long rescuerId = getCurrentUserId(authentication);
        return ResponseEntity.ok(rescuerTaskService.getMyTaskGroups(rescuerId, status, pageable));
    }

    @GetMapping({"/tasks/{id}", "/task-groups/{id}"})
    public ResponseEntity<TaskGroupResponse> getMyTaskGroupById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long rescuerId = getCurrentUserId(authentication);
        return ResponseEntity.ok(rescuerTaskService.getMyTaskGroup(rescuerId, id));
    }

    @PutMapping({"/tasks/{id}/status", "/task-groups/{id}/status"})
    public ResponseEntity<TaskGroupResponse> updateMyTaskGroupStatus(
            @PathVariable Long id,
            @RequestParam TaskGroupStatus status,
            @RequestParam(required = false) String note,
            Authentication authentication
    ) {
        Long rescuerId = getCurrentUserId(authentication);
        return ResponseEntity.ok(rescuerTaskService.updateMyTaskGroupStatus(rescuerId, id, status, note));
    }
}
