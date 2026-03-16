package com.floodrescue.module.rescue.controller;

import com.floodrescue.module.rescue.dto.request.AddNoteRequest;
import com.floodrescue.module.rescue.dto.request.ConfirmRescueResultRequest;
import com.floodrescue.module.rescue.dto.request.RescueRequestCreateRequest;
import com.floodrescue.module.rescue.dto.request.ReopenCancelledRequest;
import com.floodrescue.module.rescue.dto.request.RescueRequestUpdateRequest;
import com.floodrescue.module.rescue.dto.response.AttachmentUploadResponse;
import com.floodrescue.module.rescue.dto.response.CitizenRescueConfirmationResponse;
import com.floodrescue.module.rescue.dto.response.RescueRequestResponse;
import com.floodrescue.module.rescue.service.RescueRequestService;
import com.floodrescue.shared.dto.ApiResult;
import com.floodrescue.shared.dto.PagedData;
import com.floodrescue.shared.exception.ForbiddenException;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/rescue/citizen")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CITIZEN')")
public class CitizenRescueController {

    private final RescueRequestService rescueRequestService;

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return Long.parseLong(userDetails.getUsername());
    }

    @PostMapping("/requests")
    public ResponseEntity<ApiResult<RescueRequestResponse>> createRescueRequest(
            @Valid @RequestBody RescueRequestCreateRequest request,
            Authentication authentication) {
        Long citizenId = getCurrentUserId(authentication);
        RescueRequestResponse response = rescueRequestService.createRescueRequest(citizenId, request);
        return ResponseEntity.ok(ApiResult.ok("Tạo yêu cầu cứu hộ thành công", response));
    }

    @GetMapping("/requests")
    public ResponseEntity<ApiResult<PagedData<RescueRequestResponse>>> getMyRescueRequests(
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {
        Long citizenId = getCurrentUserId(authentication);
        Page<RescueRequestResponse> response = rescueRequestService.getRescueRequestsByCitizen(citizenId, pageable);
        return ResponseEntity.ok(ApiResult.ok(PagedData.from(response)));
    }

    @GetMapping("/requests/{id}")
    public ResponseEntity<ApiResult<RescueRequestResponse>> getRescueRequestById(
            @PathVariable Long id,
            Authentication authentication) {
        Long citizenId = getCurrentUserId(authentication);
        RescueRequestResponse response = rescueRequestService.getRescueRequestById(id);
        if (!response.getCitizenId().equals(citizenId)) {
            throw new ForbiddenException("Bạn không có quyền xem yêu cầu cứu hộ này");
        }
        return ResponseEntity.ok(ApiResult.ok(response));
    }

    @PutMapping("/requests/{id}")
    public ResponseEntity<ApiResult<RescueRequestResponse>> updateRescueRequest(
            @PathVariable Long id,
            @Valid @RequestBody RescueRequestUpdateRequest request,
            Authentication authentication) {
        Long citizenId = getCurrentUserId(authentication);
        RescueRequestResponse response = rescueRequestService.updateRescueRequest(id, citizenId, request);
        return ResponseEntity.ok(ApiResult.ok("Cập nhật yêu cầu cứu hộ thành công", response));
    }

    @DeleteMapping("/requests/{id}")
    public ResponseEntity<ApiResult<Void>> cancelRescueRequest(
            @PathVariable Long id,
            Authentication authentication) {
        Long citizenId = getCurrentUserId(authentication);
        rescueRequestService.cancelRescueRequest(id, citizenId);
        return ResponseEntity.ok(ApiResult.ok("Yêu cầu cứu hộ đã được hủy"));
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

    @PostMapping("/requests/{id}/confirm-result")
    public ResponseEntity<ApiResult<CitizenRescueConfirmationResponse>> confirmRescueResult(
            @PathVariable Long id,
            @Valid @RequestBody ConfirmRescueResultRequest request,
            Authentication authentication
    ) {
        Long citizenId = getCurrentUserId(authentication);
        CitizenRescueConfirmationResponse response = rescueRequestService.confirmRescueResult(
                id,
                citizenId,
                request.getRescued(),
                request.getReason()
        );
        return ResponseEntity.ok(ApiResult.ok(response));
    }

    @PostMapping("/requests/{id}/reopen")
    public ResponseEntity<ApiResult<RescueRequestResponse>> reopenCancelledRequest(
            @PathVariable Long id,
            @Valid @RequestBody ReopenCancelledRequest request,
            Authentication authentication
    ) {
        Long citizenId = getCurrentUserId(authentication);
        return ResponseEntity.ok(ApiResult.ok("Mở lại yêu cầu thành công", rescueRequestService.reopenCancelledRequest(id, citizenId, request.getReason())));
    }

    @PostMapping("/attachments")
    public ResponseEntity<ApiResult<List<AttachmentUploadResponse>>> uploadAttachments(
            @RequestParam("files") List<MultipartFile> files
    ) throws IOException {
        return ResponseEntity.ok(ApiResult.ok("Tải file lên thành công", rescueRequestService.uploadAttachments(files)));
    }
}
