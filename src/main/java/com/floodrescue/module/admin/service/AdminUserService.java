package com.floodrescue.module.admin.service;

import com.floodrescue.module.admin.dto.AdminUserCreateRequest;
import com.floodrescue.module.admin.dto.AdminUserUpdateRequest;
import com.floodrescue.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminUserService {

	private final JdbcTemplate jdbcTemplate;
	private final PasswordEncoder passwordEncoder;

	@Transactional(readOnly = true)
	public Map<String, Object> getUsers(int page, int size, String keyword, Integer roleId) {
		int safeSize = Math.max(1, Math.min(size, 100));
		int safePage = Math.max(0, page);
		int offset = safePage * safeSize;

		StringBuilder where = new StringBuilder(" WHERE 1=1 ");
		List<Object> params = new ArrayList<>();

		if (roleId != null) {
			where.append(" AND u.role_id = ? ");
			params.add(roleId);
		}
		if (keyword != null && !keyword.trim().isEmpty()) {
			where.append(" AND (u.full_name LIKE ? OR u.email LIKE ? OR u.phone LIKE ? OR CAST(u.id AS CHAR) LIKE ?) ");
			String like = "%" + keyword.trim() + "%";
			params.add(like);
			params.add(like);
			params.add(like);
			params.add(like);
		}

		Long totalUsers = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM users u " + where,
				Long.class,
				params.toArray()
		);

		List<Object> queryParams = new ArrayList<>(params);
		queryParams.add(safeSize);
		queryParams.add(offset);

		List<Map<String, Object>> users = jdbcTemplate.query(
				"SELECT u.id, u.full_name, u.email, u.phone, u.status, u.role_id, r.code AS role_code, u.created_at " +
						"FROM users u JOIN roles r ON r.id = u.role_id " +
						where +
						" ORDER BY u.id DESC LIMIT ? OFFSET ?",
				(rs, rowNum) -> {
					Map<String, Object> item = new HashMap<>();
					item.put("id", rs.getLong("id"));
					item.put("fullName", rs.getString("full_name"));
					item.put("email", rs.getString("email"));
					item.put("phone", rs.getString("phone"));
					item.put("status", rs.getInt("status") == 1 ? "ACTIVE" : "LOCKED");
					item.put("roleId", rs.getInt("role_id"));
					item.put("role", rs.getString("role_code"));
					item.put("createdAt", rs.getTimestamp("created_at"));
					return item;
				},
				queryParams.toArray()
		);

		int totalPages = (int) Math.ceil((totalUsers == null ? 0 : totalUsers) / (double) safeSize);
		if (totalPages <= 0) {
			totalPages = 1;
		}

		Map<String, Object> response = new HashMap<>();
		response.put("users", users);
		response.put("totalUsers", totalUsers == null ? 0 : totalUsers);
		response.put("totalPages", totalPages);
		response.put("page", safePage);
		return response;
	}

	@Transactional
	public void createUser(AdminUserCreateRequest request) {
		String fullName = request.getFullName().trim();
		String email = request.getEmail() == null ? "" : request.getEmail().trim().toLowerCase();
		String phone = request.getPhone() == null ? "" : request.getPhone().trim();
		String password = request.getPassword();
		Integer roleId = request.getRoleId();
		Long teamId = request.getTeamId();

		if (fullName.isEmpty() || password.isEmpty() || roleId == null) {
			throw new BusinessException("Thiếu dữ liệu bắt buộc");
		}

		if (!email.isEmpty()) {
			Integer existed = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE email = ?", Integer.class, email);
			if (existed != null && existed > 0) {
				throw new BusinessException("Email đã tồn tại");
			}
		}
		if (!phone.isEmpty()) {
			Integer existed = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE phone = ?", Integer.class, phone);
			if (existed != null && existed > 0) {
				throw new BusinessException("Số điện thoại đã tồn tại");
			}
		}

		jdbcTemplate.update(
				"INSERT INTO users(role_id, team_id, full_name, phone, email, password_hash, status, last_login_at, created_at, updated_at, failed_login_attempts, locked_at, temp_locked_until, is_leader) " +
						"VALUES (?, ?, ?, ?, ?, ?, 1, NULL, NOW(), NOW(), 0, NULL, NULL, b'0')",
				roleId, teamId, fullName, phone.isEmpty() ? null : phone, email.isEmpty() ? null : email,
				passwordEncoder.encode(password)
		);
	}

	@Transactional
	public Map<String, Object> updateUser(Long id, AdminUserUpdateRequest request) {
		Map<String, Object> before = jdbcTemplate.queryForMap("SELECT * FROM users WHERE id = ?", id);

		String fullName = request.getFullName() == null
				? String.valueOf(before.get("full_name"))
				: request.getFullName().trim();
		String email = request.getEmail() == null ? null : request.getEmail().trim().toLowerCase();
		String phone = request.getPhone() == null ? null : request.getPhone().trim();
		Integer roleId = request.getRoleId() == null
				? ((Number) before.get("role_id")).intValue()
				: request.getRoleId();
		String status = request.getStatus() == null
				? (((Number) before.get("status")).intValue() == 1 ? "ACTIVE" : "LOCKED")
				: request.getStatus();
		int statusVal = "ACTIVE".equalsIgnoreCase(status) ? 1 : 0;

		jdbcTemplate.update(
				"UPDATE users SET full_name = ?, email = ?, phone = ?, role_id = ?, status = ?, updated_at = NOW() WHERE id = ?",
				fullName, email, phone, roleId, statusVal, id
		);

		return before;
	}

	@Transactional
	public Map<String, Object> deleteUser(Long id) {
		Map<String, Object> before = jdbcTemplate.queryForMap("SELECT * FROM users WHERE id = ?", id);
		jdbcTemplate.update("DELETE FROM users WHERE id = ?", id);
		return before;
	}

	@Transactional
	public void resetPassword(Long id, String password) {
		jdbcTemplate.update("UPDATE users SET password_hash = ?, updated_at = NOW() WHERE id = ?", passwordEncoder.encode(password), id);
	}

	@Transactional
	public void updateStatus(Long id, String status) {
		int statusVal = "ACTIVE".equalsIgnoreCase(status) ? 1 : 0;
		jdbcTemplate.update("UPDATE users SET status = ?, updated_at = NOW() WHERE id = ?", statusVal, id);
	}
}
