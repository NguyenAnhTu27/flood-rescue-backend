package com.floodrescue.module.rescue.controller;

import com.floodrescue.module.rescue.dto.request.AddNoteRequest;
import com.floodrescue.module.rescue.dto.request.MarkDuplicateRequest;
import com.floodrescue.module.rescue.dto.request.PrioritizeRequest;
import com.floodrescue.module.rescue.dto.request.VerifyRequest;
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
@RequestMapping("/api/rescue/coordinator")
@RequiredArgsConstructor
public class CoordinatorRescueController {

    private final RescueRequestService rescueRequestService;

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return Long.parseLong(userDetails.getUsername());
    }

    @GetMapping("/requests")
    public ResponseEntity<Page<RescueRequestResponse>> getRescueRequests(
            @RequestParam(required = false) RescueRequestStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<RescueRequestResponse> response;
        if (status != null) {
            response = rescueRequestService.getRescueRequestsByStatus(status, pageable);
        } else {
            response = rescueRequestService.getPendingRescueRequests(pageable);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/requests/{id}")
    public ResponseEntity<RescueRequestResponse> getRescueRequestById(@PathVariable Long id) {
        RescueRequestResponse response = rescueRequestService.getRescueRequestById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/requests/code/{code}")
    public ResponseEntity<RescueRequestResponse> getRescueRequestByCode(@PathVariable String code) {
        RescueRequestResponse response = rescueRequestService.getRescueRequestByCode(code);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/requests/{id}/verify")
    public ResponseEntity<RescueRequestResponse> verifyRescueRequest(
            @PathVariable Long id,
            @Valid @RequestBody VerifyRequest request,
            Authentication authentication) {
        Long coordinatorId = getCurrentUserId(authentication);
        RescueRequestResponse response = rescueRequestService.verifyRescueRequest(id, coordinatorId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/requests/{id}/priority")
    public ResponseEntity<RescueRequestResponse> prioritizeRescueRequest(
            @PathVariable Long id,
            @Valid @RequestBody PrioritizeRequest request,
            Authentication authentication) {
        Long coordinatorId = getCurrentUserId(authentication);
        RescueRequestResponse response = rescueRequestService.prioritizeRescueRequest(id, coordinatorId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/requests/{id}/duplicate")
    public ResponseEntity<RescueRequestResponse> markAsDuplicate(
            @PathVariable Long id,
            @Valid @RequestBody MarkDuplicateRequest request,
            Authentication authentication) {
        Long coordinatorId = getCurrentUserId(authentication);
        RescueRequestResponse response = rescueRequestService.markAsDuplicate(id, coordinatorId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/requests/{id}/status")
    public ResponseEntity<RescueRequestResponse> changeStatus(
            @PathVariable Long id,
            @RequestParam RescueRequestStatus status,
            @RequestParam(required = false) String note,
            Authentication authentication) {
        Long coordinatorId = getCurrentUserId(authentication);
        RescueRequestResponse response = rescueRequestService.changeStatus(id, coordinatorId, status, note);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/requests/{id}/notes")
    public ResponseEntity<RescueRequestResponse> addNote(
            @PathVariable Long id,
            @Valid @RequestBody AddNoteRequest request,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        RescueRequestResponse response = rescueRequestService.addNote(id, userId, request);
        return ResponseEntity.ok(response);
    }
}
