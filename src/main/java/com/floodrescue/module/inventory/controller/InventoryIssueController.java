package com.floodrescue.module.inventory.controller;

import com.floodrescue.module.inventory.dto.request.InventoryIssueCreateRequest;
import com.floodrescue.module.inventory.dto.response.InventoryIssueResponse;
import com.floodrescue.module.inventory.service.IssueService;
import com.floodrescue.shared.enums.InventoryDocumentStatus;
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

@RestController
@RequestMapping("/api/inventory/issues")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class InventoryIssueController {

    private final IssueService issueService;

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return Long.parseLong(userDetails.getUsername());
    }

    @PostMapping
    public ResponseEntity<InventoryIssueResponse> createIssue(
            @Valid @RequestBody InventoryIssueCreateRequest request,
            Authentication authentication
    ) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(issueService.createIssue(userId, request));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<InventoryIssueResponse> approveIssue(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(issueService.approveIssue(id, userId));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<InventoryIssueResponse> cancelIssue(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(issueService.cancelIssue(id, userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryIssueResponse> getIssue(@PathVariable Long id) {
        return ResponseEntity.ok(issueService.getIssue(id));
    }

    @GetMapping
    public ResponseEntity<Page<InventoryIssueResponse>> listIssues(
            @RequestParam(required = false) InventoryDocumentStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(issueService.listIssues(status, pageable));
    }
}
