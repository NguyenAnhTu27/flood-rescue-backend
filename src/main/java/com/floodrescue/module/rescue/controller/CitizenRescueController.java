package com.floodrescue.module.rescue.controller;

import com.floodrescue.module.rescue.dto.request.AddNoteRequest;
import com.floodrescue.module.rescue.dto.request.RescueRequestCreateRequest;
import com.floodrescue.module.rescue.dto.request.RescueRequestUpdateRequest;
import com.floodrescue.module.rescue.dto.response.RescueRequestResponse;
import com.floodrescue.module.rescue.service.RescueRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/rescue/citizen")
@RequiredArgsConstructor
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
    public ResponseEntity<RescueRequestResponse> createRescueRequest(
            @Valid @RequestBody RescueRequestCreateRequest request,
            Authentication authentication) {
        Long citizenId = getCurrentUserId(authentication);
        RescueRequestResponse response = rescueRequestService.createRescueRequest(citizenId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/requests")
    public ResponseEntity<Page<RescueRequestResponse>> getMyRescueRequests(
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {
        Long citizenId = getCurrentUserId(authentication);
        Page<RescueRequestResponse> response = rescueRequestService.getRescueRequestsByCitizen(citizenId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/requests/{id}")
    public ResponseEntity<RescueRequestResponse> getRescueRequestById(@PathVariable Long id) {
        RescueRequestResponse response = rescueRequestService.getRescueRequestById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/requests/{id}")
    public ResponseEntity<RescueRequestResponse> updateRescueRequest(
            @PathVariable Long id,
            @Valid @RequestBody RescueRequestUpdateRequest request,
            Authentication authentication) {
        Long citizenId = getCurrentUserId(authentication);
        RescueRequestResponse response = rescueRequestService.updateRescueRequest(id, citizenId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/requests/{id}")
    public ResponseEntity<Map<String, String>> cancelRescueRequest(
            @PathVariable Long id,
            Authentication authentication) {
        Long citizenId = getCurrentUserId(authentication);
        rescueRequestService.cancelRescueRequest(id, citizenId);
        return ResponseEntity.ok(Map.of("message", "Yêu cầu cứu hộ đã được hủy"));
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
