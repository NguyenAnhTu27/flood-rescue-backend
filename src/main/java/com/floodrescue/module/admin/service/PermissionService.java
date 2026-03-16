package com.floodrescue.module.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {

	private final JdbcTemplate jdbcTemplate;

	@Transactional(readOnly = true)
	public Map<String, Object> getPermissionsOverview() {
		List<Map<String, Object>> roles = jdbcTemplate.query(
				"SELECT r.id, r.code, r.name, (SELECT COUNT(*) FROM users u WHERE u.role_id = r.id) AS user_count FROM roles r ORDER BY r.id",
				(rs, rowNum) -> {
					Map<String, Object> item = new HashMap<>();
					item.put("id", rs.getInt("id"));
					item.put("code", rs.getString("code"));
					item.put("name", rs.getString("name"));
					item.put("userCount", rs.getLong("user_count"));
					return item;
				}
		);

		List<Map<String, Object>> permissions = jdbcTemplate.query(
				"SELECT id, code, name, module FROM permissions ORDER BY module, code",
				(rs, rowNum) -> {
					Map<String, Object> item = new HashMap<>();
					item.put("id", rs.getInt("id"));
					item.put("code", rs.getString("code"));
					item.put("name", rs.getString("name"));
					item.put("module", rs.getString("module"));
					return item;
				}
		);

		Map<String, List<String>> rolePermissions = new HashMap<>();
		for (Map<String, Object> role : roles) {
			String roleCode = String.valueOf(role.get("code"));
			Integer roleId = (Integer) role.get("id");
			List<String> codes = jdbcTemplate.query(
					"SELECT p.code FROM role_permissions rp JOIN permissions p ON p.id = rp.permission_id WHERE rp.role_id = ? ORDER BY p.code",
					(rs, rowNum) -> rs.getString("code"),
					roleId
			);
			rolePermissions.put(roleCode, codes);
		}

		Map<String, Object> result = new HashMap<>();
		result.put("roles", roles);
		result.put("permissions", permissions);
		result.put("rolePermissions", rolePermissions);
		return result;
	}

	@Transactional
	public Long updateRolePermissions(String roleCode, Object rawPermissions) {
		Integer roleId = jdbcTemplate.queryForObject("SELECT id FROM roles WHERE code = ?", Integer.class, roleCode);
		if (roleId == null) {
			return null;
		}

		@SuppressWarnings("unchecked")
		List<String> codes = rawPermissions instanceof List
				? ((List<Object>) rawPermissions).stream().map(String::valueOf).collect(Collectors.toList())
				: new ArrayList<>();

		jdbcTemplate.update("DELETE FROM role_permissions WHERE role_id = ?", roleId);

		if (!codes.isEmpty()) {
			String placeholders = codes.stream().map(c -> "?").collect(Collectors.joining(","));
			List<Map<String, Object>> perms = jdbcTemplate.queryForList(
					"SELECT id, code FROM permissions WHERE code IN (" + placeholders + ")",
					codes.toArray()
			);
			for (Map<String, Object> p : perms) {
				Integer permissionId = ((Number) p.get("id")).intValue();
				jdbcTemplate.update(
						"INSERT INTO role_permissions(role_id, permission_id, created_at) VALUES (?, ?, ?)",
						roleId, permissionId, Timestamp.valueOf(LocalDateTime.now())
				);
			}
		}

		return roleId.longValue();
	}
}
