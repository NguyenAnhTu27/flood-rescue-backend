package com.floodrescue.module.admin.service;

import com.floodrescue.module.admin.dto.request.AdminCatalogGroupUpdateRequest;
import com.floodrescue.module.admin.dto.request.AdminCatalogRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AdminCatalogService {

    private final JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> getCatalogs() {
        return jdbcTemplate.query(
                "SELECT id, group_code, code, name, active, created_at, updated_at FROM admin_catalogs ORDER BY group_code, code",
                (rs, rowNum) -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("id", rs.getLong("id"));
                    row.put("groupCode", rs.getString("group_code"));
                    row.put("code", rs.getString("code"));
                    row.put("name", rs.getString("name"));
                    row.put("nameVn", rs.getString("name"));
                    row.put("active", rs.getBoolean("active"));
                    row.put("createdAt", rs.getTimestamp("created_at"));
                    row.put("updatedAt", rs.getTimestamp("updated_at"));
                    return row;
                }
        );
    }

    public ResponseEntity<Map<String, Object>> createCatalog(AdminCatalogRequest payload) {
        String groupCode = payload.getGroupCode().trim().toUpperCase();
        String code = payload.getCode().trim().toUpperCase();
        String name = resolveCatalogName(payload);
        boolean active = payload.getActive() == null || payload.getActive();

        jdbcTemplate.update(
                "INSERT INTO admin_catalogs(group_code, code, name, active, created_at, updated_at) VALUES (?, ?, ?, ?, NOW(), NOW())",
                groupCode, code, name, active ? 1 : 0
        );
        return ResponseEntity.ok(Map.of("message", "Tạo catalog thành công"));
    }

    public ResponseEntity<Map<String, Object>> updateCatalog(Long id, AdminCatalogRequest payload) {
        String groupCode = payload.getGroupCode().trim().toUpperCase();
        String code = payload.getCode().trim().toUpperCase();
        String name = resolveCatalogName(payload);
        boolean active = payload.getActive() == null || payload.getActive();

        jdbcTemplate.update(
                "UPDATE admin_catalogs SET group_code = ?, code = ?, name = ?, active = ?, updated_at = NOW() WHERE id = ?",
                groupCode, code, name, active ? 1 : 0, id
        );
        return ResponseEntity.ok(Map.of("message", "Cập nhật catalog thành công"));
    }

    public ResponseEntity<Map<String, Object>> deleteCatalog(Long id) {
        jdbcTemplate.update("DELETE FROM admin_catalogs WHERE id = ?", id);
        return ResponseEntity.ok(Map.of("message", "Xóa catalog thành công"));
    }

    public ResponseEntity<Map<String, Object>> toggleCatalogActive(Long id) {
        jdbcTemplate.update("UPDATE admin_catalogs SET active = IF(active = 1, 0, 1), updated_at = NOW() WHERE id = ?", id);
        return ResponseEntity.ok(Map.of("message", "Đã cập nhật trạng thái"));
    }

    public List<Map<String, Object>> getCatalogGroups() {
        return jdbcTemplate.query(
                "SELECT group_code, MAX(CASE WHEN code='__GROUP__' THEN name ELSE group_code END) AS display_name, " +
                        "SUM(CASE WHEN code<>'__GROUP__' THEN 1 ELSE 0 END) AS total_statuses " +
                        "FROM admin_catalogs GROUP BY group_code ORDER BY group_code",
                (rs, rowNum) -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("groupCode", rs.getString("group_code"));
                    item.put("name", rs.getString("display_name"));
                    item.put("totalStatuses", rs.getLong("total_statuses"));
                    return item;
                }
        );
    }

    public ResponseEntity<Map<String, Object>> updateCatalogGroupName(String groupCode, AdminCatalogGroupUpdateRequest payload) {
        String normalized = groupCode.trim().toUpperCase();
        String name = payload.getName().trim();

        Integer cnt = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM admin_catalogs WHERE group_code = ? AND code = '__GROUP__'",
                Integer.class,
                normalized
        );
        if (cnt != null && cnt > 0) {
            jdbcTemplate.update(
                    "UPDATE admin_catalogs SET name = ?, updated_at = NOW() WHERE group_code = ? AND code = '__GROUP__'",
                    name, normalized
            );
        } else {
            jdbcTemplate.update(
                    "INSERT INTO admin_catalogs(group_code, code, name, active, created_at, updated_at) VALUES (?, '__GROUP__', ?, 1, NOW(), NOW())",
                    normalized, name
            );
        }
        return ResponseEntity.ok(Map.of("message", "Cập nhật nhóm danh mục thành công"));
    }

    public ResponseEntity<Map<String, Object>> deleteCatalogGroup(String groupCode) {
        jdbcTemplate.update("DELETE FROM admin_catalogs WHERE group_code = ?", groupCode.trim().toUpperCase());
        return ResponseEntity.ok(Map.of("message", "Xóa nhóm danh mục thành công"));
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String resolveCatalogName(AdminCatalogRequest payload) {
        String nameVn = normalizeOptional(payload.getNameVn());
        if (nameVn != null) {
            return nameVn;
        }
        return Objects.requireNonNull(normalizeOptional(payload.getName()));
    }
}
