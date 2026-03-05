package com.swp391.rescuemanagement.controller;

import com.swp391.rescuemanagement.dto.request.CreateRescueRequestRequest;
import com.swp391.rescuemanagement.dto.request.UpdateStatusRequest;
import com.swp391.rescuemanagement.dto.response.ApiResponse;
import com.swp391.rescuemanagement.dto.response.RescueRequestResponse;
import com.swp391.rescuemanagement.model.User;
import com.swp391.rescuemanagement.service.AuthService;
import com.swp391.rescuemanagement.service.RescueRequestService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rescue-requests")
@RequiredArgsConstructor
public class RescueRequestController {

    private final RescueRequestService rescueRequestService;
    private final AuthService authService;

    /**
     * POST /api/rescue-requests
     * Tạo yêu cầu cứu hộ mới.
     *
     * 201 → Tạo thành công
     * 401 → Chưa đăng nhập
     * 409 → Caller đã có pending request
     */
    @PostMapping
    public ResponseEntity<ApiResponse<RescueRequestResponse>> create(
            @Valid @RequestBody CreateRescueRequestRequest dto,
            HttpServletRequest httpRequest) {

        User caller = authService.getCurrentUser(httpRequest);
        RescueRequestResponse result = rescueRequestService.create(dto, caller);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(result, "Tạo yêu cầu cứu hộ thành công"));
    }

    /**
     * GET /api/rescue-requests?status=pending
     * Lấy danh sách (tùy chọn filter theo status).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<RescueRequestResponse>>> getAll(
            @RequestParam(required = false) String status) {

        List<RescueRequestResponse> list = (status != null && !status.isBlank())
                ? rescueRequestService.getByStatus(status)
                : rescueRequestService.getAll();

        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    /**
     * GET /api/rescue-requests/{id}
     * Lấy chi tiết 1 rescue request.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RescueRequestResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(rescueRequestService.getById(id)));
    }

    /**
     * PUT /api/rescue-requests/{id}/status
     * BƯỚC 4: Cập nhật status rescue_request.
     * Body: UpdateStatusRequest { status: "in_progress" | "completed" | "cancelled"
     * }
     *
     * 200 → Cập nhật thành công
     * 400 → Chuyển trạng thái không hợp lệ
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<RescueRequestResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest dto,
            HttpServletRequest httpRequest) {

        User actor = authService.getCurrentUser(httpRequest);
        RescueRequestResponse result = rescueRequestService.updateStatus(id, dto.status(), actor);
        return ResponseEntity.ok(ApiResponse.ok(result, "Cập nhật trạng thái thành công"));
    }
}
