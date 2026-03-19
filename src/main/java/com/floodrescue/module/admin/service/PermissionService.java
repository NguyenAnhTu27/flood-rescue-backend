package com.floodrescue.module.admin.service;

import com.floodrescue.module.admin.dto.request.AdminUpdateRolePermissionsRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final JdbcTemplate jdbcTemplate;
    private final AuditLogService auditLogService;

    public Map<String, Object> getPermissions() {
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

    public ResponseEntity<Map<String, Object>> updateRolePermissions(
            String roleCode,
            AdminUpdateRolePermissionsRequest payload,
            Long actorId,
            HttpServletRequest request
    ) {
        Integer roleId = jdbcTemplate.queryForObject("SELECT id FROM roles WHERE code = ?", Integer.class, roleCode);
        if (roleId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Vai trò không tồn tại"));
        }

        List<String> codes = payload.getPermissions() == null
                ? new ArrayList<>()
                : payload.getPermissions().stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());

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

        auditLogService.writeAudit(actorId, "UPDATE_ROLE_PERMISSIONS", "ROLE", roleId.longValue(), "SUCCESS", "Cập nhật phân quyền", roleCode, request, null, payload);
        return ResponseEntity.ok(Map.of("message", "Cập nhật phân quyền thành công"));
    }
}
