package com.floodrescue.module.admin.controller;

import com.floodrescue.module.admin.service.AdminUserService;
import com.floodrescue.module.admin.service.AuditLogService;
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
			@RequestBody Map<String, Object> payload,
			Authentication authentication,
			HttpServletRequest request
	) {
		adminUserService.createUser(payload);
		Long actorId = getCurrentUserId(authentication);
		String email = String.valueOf(payload.getOrDefault("email", "")).trim().toLowerCase();
		auditLogService.writeAudit(actorId, "CREATE_USER", "USER", null, "SUCCESS", "Tạo tài khoản mới", email, request, null, payload);
		return ResponseEntity.ok(ApiResult.ok("Tạo tài khoản thành công"));
	}

	@PutMapping("/users/{id}")
	public ResponseEntity<ApiResult<Void>> updateUser(
			@PathVariable Long id,
			@RequestBody Map<String, Object> payload,
			Authentication authentication,
			HttpServletRequest request
	) {
		Map<String, Object> before = adminUserService.updateUser(id, payload);
		String email = payload.get("email") == null ? null : String.valueOf(payload.get("email")).trim().toLowerCase();

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
			@RequestBody Map<String, Object> payload,
			Authentication authentication,
			HttpServletRequest request
	) {
		String password = String.valueOf(payload.getOrDefault("password", "")).trim();
		if (password.isEmpty()) {
			return ResponseEntity.badRequest().body(ApiResult.error("Mật khẩu mới không được để trống"));
		}

		adminUserService.resetPassword(id, password);
		Long actorId = getCurrentUserId(authentication);
		auditLogService.writeAudit(actorId, "RESET_PASSWORD", "USER", id, "SUCCESS", "Reset mật khẩu người dùng", null, request, null, Map.of("id", id));
		return ResponseEntity.ok(ApiResult.ok("Reset password thành công"));
	}

	@PutMapping("/users/{id}/status")
	public ResponseEntity<ApiResult<Void>> updateStatus(
			@PathVariable Long id,
			@RequestBody Map<String, Object> payload,
			Authentication authentication,
			HttpServletRequest request
	) {
		String status = String.valueOf(payload.getOrDefault("status", "ACTIVE"));
		adminUserService.updateStatus(id, status);
		Long actorId = getCurrentUserId(authentication);
		auditLogService.writeAudit(actorId, "UPDATE_STATUS", "USER", id, "SUCCESS", "Cập nhật trạng thái người dùng", status, request, null, payload);
		return ResponseEntity.ok(ApiResult.ok("Cập nhật trạng thái thành công"));
	}
}
