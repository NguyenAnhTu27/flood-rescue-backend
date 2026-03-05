package com.swp391.rescuemanagement.controller;

import com.swp391.rescuemanagement.dto.request.AssignTeamRequest;
import com.swp391.rescuemanagement.dto.request.UpdateStatusRequest;
import com.swp391.rescuemanagement.dto.response.ApiResponse;
import com.swp391.rescuemanagement.dto.response.RescueAssignmentResponse;
import com.swp391.rescuemanagement.model.User;
import com.swp391.rescuemanagement.service.AuthService;
import com.swp391.rescuemanagement.service.RescueAssignmentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class RescueAssignmentController {

    private final RescueAssignmentService assignmentService;
    private final AuthService authService;

    /**
     * POST /api/assignments
     * Gán team (qua TaskGroup) vào rescue request.
     *
     * Body: AssignTeamRequest { rescueRequestId, taskGroupId, assetId? }
     * 201 → Gán thành công
     * 400 → Request không 'pending' hoặc TaskGroup không 'idle'
     * 409 → Team đang bận
     */
    @PostMapping
    public ResponseEntity<ApiResponse<RescueAssignmentResponse>> assign(
            @Valid @RequestBody AssignTeamRequest dto,
            HttpServletRequest httpRequest) {

        User actor = authService.getCurrentUser(httpRequest);
        RescueAssignmentResponse result = assignmentService.assignTeam(dto, actor);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(result, "Gán team thành công"));
    }

    /**
     * PUT /api/assignments/{id}/status
     * BƯỚC 4: Cập nhật tiến độ assignment.
     *
     * Body: UpdateStatusRequest { status: "in_progress" | "completed" | "cancelled"
     * }
     *
     * in_progress → team đang bận, rescue_request chuyển in_progress
     * completed → team được giải phóng (rảnh), rescue_request chuyển completed
     * cancelled → TaskGroup về idle
     *
     * 200 → Cập nhật thành công
     * 400 → Chuyển trạng thái không hợp lệ
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<RescueAssignmentResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest dto,
            HttpServletRequest httpRequest) {

        User actor = authService.getCurrentUser(httpRequest);
        RescueAssignmentResponse result = assignmentService.updateStatus(id, dto.status(), actor);
        return ResponseEntity.ok(ApiResponse.ok(result, "Cập nhật trạng thái thành công"));
    }

    /**
     * GET /api/assignments?taskGroupId={id}
     * Lấy danh sách assignment của 1 TaskGroup.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<RescueAssignmentResponse>>> getByTaskGroup(
            @RequestParam Long taskGroupId) {

        return ResponseEntity.ok(
                ApiResponse.ok(assignmentService.getByTaskGroup(taskGroupId)));
    }
}
