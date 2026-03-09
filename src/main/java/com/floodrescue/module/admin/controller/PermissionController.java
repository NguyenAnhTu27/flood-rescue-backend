package com.floodrescue.module.admin.controller;

import com.floodrescue.module.admin.dto.request.UpdateRolePermissionsRequest;
import com.floodrescue.module.admin.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping("/permissions")
    public ResponseEntity<?> getPermissionMatrix() {
        return ResponseEntity.ok(permissionService.getPermissionMatrix());
    }

    @GetMapping("/roles/{roleCode}/permissions")
    public ResponseEntity<?> getPermissionsByRole(@PathVariable String roleCode) {
        return ResponseEntity.ok(Map.of(
                "role", roleCode.toUpperCase(),
                "permissions", permissionService.getPermissionsByRole(roleCode)
        ));
    }

    @PutMapping("/roles/{roleCode}/permissions")
    public ResponseEntity<?> updateRolePermissions(
            @PathVariable String roleCode,
            @RequestBody UpdateRolePermissionsRequest request
    ) {
        return ResponseEntity.ok(permissionService.updateRolePermissions(roleCode, request));
    }
}
