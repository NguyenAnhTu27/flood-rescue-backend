package com.floodrescue.module.rescue.controller;

import com.floodrescue.module.rescue.dto.request.AddNoteRequest;
import com.floodrescue.module.rescue.dto.response.RescueRequestResponse;
import com.floodrescue.module.rescue.service.RescueRequestService;
import com.floodrescue.shared.enums.RescueRequestStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rescue/rescuer")
@RequiredArgsConstructor
public class RescuerTaskController {

    private final RescueRequestService rescueRequestService;

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return Long.parseLong(userDetails.getUsername());
    }

    @GetMapping("/tasks")
    public ResponseEntity<Page<RescueRequestResponse>> getMyTasks(
            @RequestParam(required = false) RescueRequestStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<RescueRequestResponse> response;
        if (status != null) {
            response = rescueRequestService.getRescueRequestsByStatus(status, pageable);
        } else {
            response = rescueRequestService.getRescueRequestsByStatus(RescueRequestStatus.IN_PROGRESS, pageable);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tasks/{id}")
    public ResponseEntity<RescueRequestResponse> getTaskById(@PathVariable Long id) {
        RescueRequestResponse response = rescueRequestService.getRescueRequestById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/tasks/{id}/status")
    public ResponseEntity<RescueRequestResponse> updateTaskStatus(
            @PathVariable Long id,
            @RequestParam RescueRequestStatus status,
            @RequestParam(required = false) String note,
            Authentication authentication) {
        Long rescuerId = getCurrentUserId(authentication);
        RescueRequestResponse response = rescueRequestService.changeStatus(id, rescuerId, status, note);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/tasks/{id}/notes")
    public ResponseEntity<RescueRequestResponse> addNote(
            @PathVariable Long id,
            @Valid @RequestBody AddNoteRequest request,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        RescueRequestResponse response = rescueRequestService.addNote(id, userId, request);
        return ResponseEntity.ok(response);
    }
}
