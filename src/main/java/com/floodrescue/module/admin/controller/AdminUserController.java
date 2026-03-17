package com.floodrescue.module.admin.controller;

import com.floodrescue.module.admin.dto.AdminUserCreateRequest;
import com.floodrescue.module.admin.dto.AdminUserResetPasswordRequest;
import com.floodrescue.module.admin.dto.AdminUserUpdateRequest;
import com.floodrescue.module.admin.dto.AdminUserUpdateStatusRequest;
import com.floodrescue.module.admin.service.AdminUserService;
import com.floodrescue.module.admin.service.AuditLogService;
import com.floodrescue.shared.dto.ApiResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController extends AbstractAdminController {

	private final AdminUserService adminUserService;
	private final AuditLogService auditLogService;

	@GetMapping("/users")
	public ResponseEntity<ApiResult<Map<String, Object>>> getUsers(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size,
			@RequestParam(required = false) String keyword,
			@RequestParam(required = false) Integer roleId
	) {
		return ResponseEntity.ok(ApiResult.ok(adminUserService.getUsers(page, size, keyword, roleId)));
	}

	@PostMapping("/create-user")
	public ResponseEntity<ApiResult<Void>> createUser(
			@Valid @RequestBody AdminUserCreateRequest payload,
			Authentication authentication,
			HttpServletRequest request
	) {
		adminUserService.createUser(payload);
		Long actorId = getCurrentUserId(authentication);
		String email = payload.getEmail() == null ? "" : payload.getEmail().trim().toLowerCase();
		Map<String, Object> changeSet = new java.util.LinkedHashMap<>();
		changeSet.put("fullName", payload.getFullName());
		changeSet.put("email", payload.getEmail());
		changeSet.put("phone", payload.getPhone());
		changeSet.put("roleId", payload.getRoleId());
		changeSet.put("teamId", payload.getTeamId());
		auditLogService.writeAudit(actorId, "CREATE_USER", "USER", null, "SUCCESS", "Tạo tài khoản mới", email, request, null, changeSet);
		return ResponseEntity.ok(ApiResult.ok("Tạo tài khoản thành công"));
	}

	@PutMapping("/users/{id}")
	public ResponseEntity<ApiResult<Void>> updateUser(
			@PathVariable Long id,
			@Valid @RequestBody AdminUserUpdateRequest payload,
			Authentication authentication,
			HttpServletRequest request
	) {
		Map<String, Object> before = adminUserService.updateUser(id, payload);
		String email = payload.getEmail() == null ? null : payload.getEmail().trim().toLowerCase();

		Long actorId = getCurrentUserId(authentication);
		auditLogService.writeAudit(actorId, "UPDATE_USER", "USER", id, "SUCCESS", "Cập nhật thông tin người dùng", email, request, before, payload);
		return ResponseEntity.ok(ApiResult.ok("Cập nhật người dùng thành công"));
	}

	@DeleteMapping("/users/{id}")
	public ResponseEntity<ApiResult<Void>> deleteUser(
			@PathVariable Long id,
			Authentication authentication,
			HttpServletRequest request
	) {
		Map<String, Object> before = adminUserService.deleteUser(id);
		Long actorId = getCurrentUserId(authentication);
		auditLogService.writeAudit(actorId, "DELETE_USER", "USER", id, "WARN", "Xóa tài khoản", String.valueOf(before.get("email")), request, before, null);
		return ResponseEntity.ok(ApiResult.ok("Đã xoá user"));
	}

	@PutMapping("/users/{id}/reset-password")
	public ResponseEntity<ApiResult<Void>> resetPassword(
			@PathVariable Long id,
			@Valid @RequestBody AdminUserResetPasswordRequest payload,
			Authentication authentication,
			HttpServletRequest request
	) {
		adminUserService.resetPassword(id, payload.getPassword().trim());
		Long actorId = getCurrentUserId(authentication);
		auditLogService.writeAudit(actorId, "RESET_PASSWORD", "USER", id, "SUCCESS", "Reset mật khẩu người dùng", null, request, null, Map.of("id", id));
		return ResponseEntity.ok(ApiResult.ok("Reset password thành công"));
	}

	@PutMapping("/users/{id}/status")
	public ResponseEntity<ApiResult<Void>> updateStatus(
			@PathVariable Long id,
			@Valid @RequestBody AdminUserUpdateStatusRequest payload,
			Authentication authentication,
			HttpServletRequest request
	) {
		String status = payload.getStatus();
		adminUserService.updateStatus(id, status);
		Long actorId = getCurrentUserId(authentication);
		auditLogService.writeAudit(actorId, "UPDATE_STATUS", "USER", id, "SUCCESS", "Cập nhật trạng thái người dùng", status, request, null, Map.of("status", status));
		return ResponseEntity.ok(ApiResult.ok("Cập nhật trạng thái thành công"));
	}
}
