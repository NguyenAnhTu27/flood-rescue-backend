package com.floodrescue.module.admin.controller;

import com.floodrescue.module.admin.service.AuditLogService;
import com.floodrescue.module.admin.service.PermissionService;
import com.floodrescue.shared.dto.ApiResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PermissionController extends AbstractAdminController {

	private final AuditLogService auditLogService;
	private final PermissionService permissionService;

	@GetMapping("/permissions")
	public ResponseEntity<ApiResult<Map<String, Object>>> getPermissions() {
		return ResponseEntity.ok(ApiResult.ok(permissionService.getPermissionsOverview()));
	}

	@PutMapping("/roles/{roleCode}/permissions")
	public ResponseEntity<ApiResult<Void>> updateRolePermissions(
			@PathVariable String roleCode,
			@RequestBody Map<String, Object> payload,
			Authentication authentication,
			HttpServletRequest request
	) {
		Long roleId = permissionService.updateRolePermissions(roleCode, payload.get("permissions"));
		if (roleId == null) {
			return ResponseEntity.badRequest().body(ApiResult.error("Vai trò không tồn tại"));
		}
		Long actorId = getCurrentUserId(authentication);
		auditLogService.writeAudit(actorId, "UPDATE_ROLE_PERMISSIONS", "ROLE", roleId, "SUCCESS", "Cập nhật phân quyền", roleCode, request, null, payload);
		return ResponseEntity.ok(ApiResult.ok("Cập nhật phân quyền thành công"));
	}
}
