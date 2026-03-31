package com.floodrescue.module.admin.service;

import com.floodrescue.module.admin.dto.request.AdminCreateUserRequest;
import com.floodrescue.module.admin.dto.request.AdminResetPasswordRequest;
import com.floodrescue.module.admin.dto.request.AdminUpdateUserRequest;
import com.floodrescue.module.admin.dto.request.AdminUpdateUserStatusRequest;
import com.floodrescue.module.user.entity.UserEntity;
import com.floodrescue.module.user.repository.UserRepository;
import com.floodrescue.shared.exception.NotFoundException;
import com.floodrescue.shared.util.PhoneUtil;
import com.floodrescue.shared.util.TextNormalizationUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.sql.Timestamp;
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
    private final UserRepository userRepository;

    public Map<String, Object> getUsers(int page, int size, String keyword, Integer roleId) {
        int safeSize = Math.max(1, Math.min(size, 100));
        int safePage = Math.max(0, page);
        int offset = safePage * safeSize;
        String normalizedKeyword = keyword == null ? "" : TextNormalizationUtil.cleanDisplayText(keyword).trim().toLowerCase();

        List<Map<String, Object>> filtered = loadUserRows().stream()
                .filter(user -> roleId == null || roleId.equals(user.get("roleId")))
                .filter(user -> matchesKeyword(user, normalizedKeyword))
                .toList();

        long totalUsers = filtered.size();
        List<Map<String, Object>> users = filtered.stream()
                .skip(offset)
                .limit(safeSize)
                .toList();

        int totalPages = (int) Math.ceil(totalUsers / (double) safeSize);
        if (totalPages <= 0) {
            totalPages = 1;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("users", users);
        response.put("totalUsers", totalUsers);
        response.put("totalPages", totalPages);
        response.put("page", safePage);
        return response;
    }

    private List<Map<String, Object>> loadUserRows() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT
                    u.id,
                    u.full_name,
                    u.email,
                    u.phone,
                    u.status,
                    u.created_at,
                    r.id AS role_id,
                    r.code AS role_code
                FROM users u
                JOIN roles r ON r.id = u.role_id
                ORDER BY u.id DESC
                """);

        List<Map<String, Object>> users = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", toLong(row.get("id")));
            item.put("fullName", TextNormalizationUtil.cleanDisplayText(stringValue(row.get("full_name"))));
            item.put("email", stringValue(row.get("email")));
            item.put("phone", stringValue(row.get("phone")));
            item.put("status", toInt(row.get("status")) == 1 ? "ACTIVE" : "LOCKED");
            item.put("roleId", toInt(row.get("role_id")));
            item.put("role", stringValue(row.get("role_code")));
            item.put("createdAt", toLocalDateTime(row.get("created_at")));
            users.add(item);
        }
        return users;
    }

    public ResponseEntity<Map<String, Object>> createUser(
            AdminCreateUserRequest payload,
            Long actorId,
            HttpServletRequest request
    ) {
        String fullName = TextNormalizationUtil.cleanDisplayText(payload.getFullName()).trim();
        String email = normalizeOptional(payload.getEmail());
        if (email != null) {
            email = email.toLowerCase();
        }
        String phone = normalizeOptionalPhone(payload.getPhone());

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

        Long createdUserId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM users", Long.class);
        auditLogService.writeAudit(
                actorId,
                "CREATE_USER",
                "USER",
                createdUserId,
                "SUCCESS",
                "Tạo tài khoản mới",
                email,
                request,
                null,
                sanitizePayloadForAudit(payload)
        );
        return ResponseEntity.ok(Map.of("message", "Tạo tài khoản thành công"));
    }

    public ResponseEntity<Map<String, Object>> updateUser(
            Long id,
            AdminUpdateUserRequest payload,
            Long actorId,
            HttpServletRequest request
    ) {
        UserEntity beforeUser = findUser(id);
        Map<String, Object> before = toAuditUserSnapshot(beforeUser);

        String fullName = payload.getFullName() == null
                ? beforeUser.getFullName()
                : TextNormalizationUtil.cleanDisplayText(payload.getFullName()).trim();
        String email = payload.getEmail() == null ? null : normalizeOptional(payload.getEmail());
        if (email != null) {
            email = email.toLowerCase();
        }
        String phone = payload.getPhone() == null ? null : normalizeOptionalPhone(payload.getPhone());
        Integer roleId = payload.getRoleId() == null
                ? beforeUser.getRole().getId()
                : payload.getRoleId();
        String status = payload.getStatus() == null
                ? (beforeUser.getStatus() == 1 ? "ACTIVE" : "LOCKED")
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

        auditLogService.writeAudit(
                actorId,
                "UPDATE_USER",
                "USER",
                id,
                "SUCCESS",
                "Cập nhật thông tin người dùng",
                email,
                request,
                before,
                sanitizePayloadForAudit(payload)
        );
        return ResponseEntity.ok(Map.of("message", "Cập nhật người dùng thành công"));
    }

    public ResponseEntity<Map<String, Object>> deleteUser(Long id, Long actorId, HttpServletRequest request) {
        UserEntity beforeUser = findUser(id);
        Map<String, Object> before = toAuditUserSnapshot(beforeUser);
        jdbcTemplate.update("DELETE FROM users WHERE id = ?", id);
        auditLogService.writeAudit(actorId, "DELETE_USER", "USER", id, "WARN", "Xóa tài khoản", String.valueOf(before.get("email")), request, before, null);
        return ResponseEntity.ok(Map.of("message", "Đã xóa user"));
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

    private String normalizeOptionalPhone(String value) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            return null;
        }
        String phone = PhoneUtil.normalize(normalized);
        return phone != null ? phone : normalized;
    }

    private boolean matchesKeyword(Map<String, Object> user, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        String searchable = String.join(" ",
                stringValue(user.get("fullName")),
                stringValue(user.get("email")),
                stringValue(user.get("phone")),
                stringValue(user.get("role")),
                stringValue(user.get("id"))
        ).toLowerCase();
        return searchable.contains(keyword);
    }

    private UserEntity findUser(Long id) {
        return userRepository.findByIdWithRole(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));
    }

    private Map<String, Object> toAuditUserSnapshot(UserEntity user) {
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("id", user.getId());
        snapshot.put("fullName", TextNormalizationUtil.cleanDisplayText(user.getFullName()));
        snapshot.put("email", user.getEmail());
        snapshot.put("phone", user.getPhone());
        snapshot.put("roleId", user.getRole() != null ? user.getRole().getId() : null);
        snapshot.put("role", user.getRole() != null ? user.getRole().getCode() : null);
        snapshot.put("status", user.getStatus() == 1 ? "ACTIVE" : "LOCKED");
        snapshot.put("teamId", user.getTeamId());
        snapshot.put("isLeader", user.getIsLeader());
        snapshot.put("createdAt", user.getCreatedAt());
        snapshot.put("updatedAt", user.getUpdatedAt());
        snapshot.put("lastLoginAt", user.getLastLoginAt());
        snapshot.put("rescueRequestBlocked", user.getRescueRequestBlocked());
        snapshot.put("rescueRequestBlockedReason", TextNormalizationUtil.cleanDisplayText(user.getRescueRequestBlockedReason()));
        return snapshot;
    }

    private Map<String, Object> sanitizePayloadForAudit(AdminCreateUserRequest payload) {
        Map<String, Object> result = new HashMap<>();
        result.put("fullName", TextNormalizationUtil.cleanDisplayText(payload.getFullName()));
        result.put("email", normalizeOptional(payload.getEmail()));
        result.put("phone", normalizeOptionalPhone(payload.getPhone()));
        result.put("roleId", payload.getRoleId());
        result.put("teamId", payload.getTeamId());
        result.put("contactProvided", payload.isContactProvided());
        result.put("emailValidIfPresent", payload.isEmailValidIfPresent());
        result.put("phoneValidIfPresent", payload.isPhoneValidIfPresent());
        return result;
    }

    private Map<String, Object> sanitizePayloadForAudit(AdminUpdateUserRequest payload) {
        Map<String, Object> result = new HashMap<>();
        result.put("fullName", payload.getFullName() == null ? null : TextNormalizationUtil.cleanDisplayText(payload.getFullName()));
        result.put("email", normalizeOptional(payload.getEmail()));
        result.put("phone", normalizeOptionalPhone(payload.getPhone()));
        result.put("roleId", payload.getRoleId());
        result.put("status", payload.getStatus());
        result.put("emailValidIfPresent", payload.isEmailValidIfPresent());
        result.put("phoneValidIfPresent", payload.isPhoneValidIfPresent());
        result.put("fullNameValidIfPresent", payload.isFullNameValidIfPresent());
        return result;
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private Integer toInt(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        return LocalDateTime.parse(String.valueOf(value).replace(" ", "T"));
    }
}
