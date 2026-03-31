package com.floodrescue.module.admin.service;

import com.floodrescue.module.admin.dto.request.AdminCreateUserRequest;
import com.floodrescue.module.admin.dto.request.AdminResetPasswordRequest;
import com.floodrescue.module.admin.dto.request.AdminUpdateUserRequest;
import com.floodrescue.module.admin.dto.request.AdminUpdateUserStatusRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

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

    public ResponseEntity<Map<String, Object>> createUser(
            AdminCreateUserRequest payload,
            Long actorId,
            HttpServletRequest request
    ) {
        String fullName = payload.getFullName().trim();
        String email = normalizeOptional(payload.getEmail());
        if (email != null) {
            email = email.toLowerCase();
        }
        String phone = normalizeOptional(payload.getPhone());

        if (email != null) {
            Integer existed = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE email = ?", Integer.class, email);
            if (existed != null && existed > 0) {
                return ResponseEntity.badRequest().body(Map.of("message", "Email đã tồn tại"));
            }
        }
        if (phone != null) {
            Integer existed = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE phone = ?", Integer.class, phone);
            if (existed != null && existed > 0) {
                return ResponseEntity.badRequest().body(Map.of("message", "Số điện thoại đã tồn tại"));
            }
        }

        jdbcTemplate.update(
                "INSERT INTO users(role_id, team_id, full_name, phone, email, password_hash, status, last_login_at, created_at, updated_at, failed_login_attempts, locked_at, temp_locked_until, is_leader) " +
                        "VALUES (?, ?, ?, ?, ?, ?, 1, NULL, NOW(), NOW(), 0, NULL, NULL, b'0')",
                payload.getRoleId(), payload.getTeamId(), fullName, phone, email,
                passwordEncoder.encode(payload.getPassword())
        );

        auditLogService.writeAudit(actorId, "CREATE_USER", "USER", null, "SUCCESS", "Tạo tài khoản mới", email, request, null, payload);
        return ResponseEntity.ok(Map.of("message", "Tạo tài khoản thành công"));
    }

    public ResponseEntity<Map<String, Object>> updateUser(
            Long id,
            AdminUpdateUserRequest payload,
            Long actorId,
            HttpServletRequest request
    ) {
        Map<String, Object> before = jdbcTemplate.queryForMap("SELECT * FROM users WHERE id = ?", id);

        String fullName = payload.getFullName() == null
                ? String.valueOf(before.get("full_name"))
                : payload.getFullName().trim();
        String email = payload.getEmail() == null ? null : normalizeOptional(payload.getEmail());
        if (email != null) {
            email = email.toLowerCase();
        }
        String phone = payload.getPhone() == null ? null : normalizeOptional(payload.getPhone());
        Integer roleId = payload.getRoleId() == null
                ? ((Number) before.get("role_id")).intValue()
                : payload.getRoleId();
        String status = payload.getStatus() == null
                ? (((Number) before.get("status")).intValue() == 1 ? "ACTIVE" : "LOCKED")
                : payload.getStatus().trim().toUpperCase();
        int statusVal = "ACTIVE".equalsIgnoreCase(status) ? 1 : 0;

        if (email != null) {
            Integer existed = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM users WHERE email = ? AND id <> ?",
                    Integer.class,
                    email,
                    id
            );
            if (existed != null && existed > 0) {
                return ResponseEntity.badRequest().body(Map.of("message", "Email đã tồn tại"));
            }
        }
        if (phone != null) {
            Integer existed = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM users WHERE phone = ? AND id <> ?",
                    Integer.class,
                    phone,
                    id
            );
            if (existed != null && existed > 0) {
                return ResponseEntity.badRequest().body(Map.of("message", "Số điện thoại đã tồn tại"));
            }
        }

        jdbcTemplate.update(
                "UPDATE users SET full_name = ?, email = ?, phone = ?, role_id = ?, status = ?, updated_at = NOW() WHERE id = ?",
                fullName, email, phone, roleId, statusVal, id
        );

        auditLogService.writeAudit(actorId, "UPDATE_USER", "USER", id, "SUCCESS", "Cập nhật thông tin người dùng", email, request, before, payload);
        return ResponseEntity.ok(Map.of("message", "Cập nhật người dùng thành công"));
    }

    public ResponseEntity<Map<String, Object>> deleteUser(Long id, Long actorId, HttpServletRequest request) {
        Map<String, Object> before = jdbcTemplate.queryForMap("SELECT * FROM users WHERE id = ?", id);
        jdbcTemplate.update("DELETE FROM users WHERE id = ?", id);
        auditLogService.writeAudit(actorId, "DELETE_USER", "USER", id, "WARN", "Xóa tài khoản", String.valueOf(before.get("email")), request, before, null);
        return ResponseEntity.ok(Map.of("message", "Đã xoá user"));
    }

    public ResponseEntity<Map<String, Object>> resetPassword(
            Long id,
            AdminResetPasswordRequest payload,
            Long actorId,
            HttpServletRequest request
    ) {
        jdbcTemplate.update(
                "UPDATE users SET password_hash = ?, updated_at = NOW() WHERE id = ?",
                passwordEncoder.encode(payload.getPassword().trim()),
                id
        );
        auditLogService.writeAudit(actorId, "RESET_PASSWORD", "USER", id, "SUCCESS", "Reset mật khẩu người dùng", null, request, null, Map.of("id", id));
        return ResponseEntity.ok(Map.of("message", "Reset password thành công"));
    }

    public ResponseEntity<Map<String, Object>> updateStatus(
            Long id,
            AdminUpdateUserStatusRequest payload,
            Long actorId,
            HttpServletRequest request
    ) {
        String status = payload.getStatus().trim().toUpperCase();
        int statusVal = "ACTIVE".equalsIgnoreCase(status) ? 1 : 0;
        jdbcTemplate.update("UPDATE users SET status = ?, updated_at = NOW() WHERE id = ?", statusVal, id);
        auditLogService.writeAudit(actorId, "UPDATE_STATUS", "USER", id, "SUCCESS", "Cập nhật trạng thái người dùng", status, request, null, payload);
        return ResponseEntity.ok(Map.of("message", "Cập nhật trạng thái thành công"));
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
