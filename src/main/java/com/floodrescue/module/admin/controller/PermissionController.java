package com.floodrescue.module.admin.controller;

import com.floodrescue.module.admin.dto.request.AdminUpdateRolePermissionsRequest;
import com.floodrescue.module.admin.service.PermissionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Validated
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping("/permissions")
    public ResponseEntity<Map<String, Object>> getPermissions() {
        return ResponseEntity.ok(permissionService.getPermissions());
    }

    @PutMapping("/roles/{roleCode}/permissions")
    public ResponseEntity<Map<String, Object>> updateRolePermissions(
            @PathVariable String roleCode,
            @Valid @RequestBody AdminUpdateRolePermissionsRequest payload,
            Authentication authentication,
            HttpServletRequest request
    ) {
        return permissionService.updateRolePermissions(roleCode, payload, getCurrentUserId(authentication), request);
    }

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return Long.parseLong(userDetails.getUsername());
    }
}
