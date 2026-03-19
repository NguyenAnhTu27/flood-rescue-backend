package com.floodrescue.module.admin.service;

import com.floodrescue.module.admin.dto.request.AdminNotificationTemplateCreateRequest;
import com.floodrescue.module.admin.dto.request.AdminNotificationTemplateUpdateRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminNotificationTemplateService {

    private final JdbcTemplate jdbcTemplate;
    private final AuditLogService auditLogService;

    public Map<String, Object> getNotificationTemplates(int page, int size, String keyword) {
        int safeSize = Math.max(1, Math.min(size, 100));
        int safePage = Math.max(0, page);
        int offset = safePage * safeSize;

        StringBuilder where = new StringBuilder(" WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            where.append(" AND (code LIKE ? OR template_key LIKE ? OR title LIKE ?) ");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }

        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM notification_templates " + where,
                Long.class,
                params.toArray()
        );
        List<Object> q = new ArrayList<>(params);
        q.add(safeSize);
        q.add(offset);
        List<Map<String, Object>> items = jdbcTemplate.query(
                "SELECT id, code, template_key, title, content, channel, IF(COALESCE(is_active,0)=1 OR COALESCE(active,0)=1,1,0) AS active_flag, created_at, updated_at " +
                        "FROM notification_templates " + where +
                        " ORDER BY id DESC LIMIT ? OFFSET ?",
                (rs, rowNum) -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", rs.getInt("id"));
                    item.put("code", rs.getString("code"));
                    item.put("templateKey", rs.getString("template_key"));
                    item.put("title", rs.getString("title"));
                    item.put("content", rs.getString("content"));
                    item.put("channel", rs.getString("channel"));
                    item.put("active", rs.getInt("active_flag") == 1);
                    item.put("createdAt", rs.getTimestamp("created_at"));
                    item.put("updatedAt", rs.getTimestamp("updated_at"));
                    return item;
                },
                q.toArray()
        );

        Long activeCnt = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM notification_templates WHERE IF(COALESCE(is_active,0)=1 OR COALESCE(active,0)=1,1,0)=1",
                Long.class
        );
        String topChannel = jdbcTemplate.query(
                "SELECT channel, COUNT(*) c FROM notification_templates GROUP BY channel ORDER BY c DESC LIMIT 1",
                rs -> rs.next() ? rs.getString("channel") : "N/A"
        );

        int totalPages = (int) Math.ceil((total == null ? 0 : total) / (double) safeSize);
        if (totalPages <= 0) {
            totalPages = 1;
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTemplates", total == null ? 0 : total);
        stats.put("activeTemplates", activeCnt == null ? 0 : activeCnt);
        stats.put("topChannel", topChannel == null ? "N/A" : topChannel);

        Map<String, Object> response = new HashMap<>();
        response.put("items", items);
        response.put("page", safePage);
        response.put("totalPages", totalPages);
        response.put("totalItems", total == null ? 0 : total);
        response.put("stats", stats);
        return response;
    }

    public ResponseEntity<Map<String, Object>> createNotificationTemplate(
            AdminNotificationTemplateCreateRequest payload,
            Long actorId,
            HttpServletRequest request
    ) {
        String code = payload.getCode().trim().toUpperCase();
        String templateKey = valueOrDefault(payload.getTemplateKey(), code).trim().toUpperCase();
        String title = payload.getTitle().trim();
        String channel = payload.getChannel().trim().toUpperCase();
        String content = payload.getContent().trim();
        boolean active = payload.getActive() == null || payload.getActive();

        jdbcTemplate.update(
                "INSERT INTO notification_templates(code, template_key, title, content, channel, is_active, active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())",
                code, templateKey, title, content, channel, active ? 1 : 0, active ? 1 : 0
        );
        auditLogService.writeAudit(actorId, "CREATE_NOTIFICATION_TEMPLATE", "NOTIFICATION_TEMPLATE", null, "SUCCESS", "Tạo mẫu thông báo", code, request, null, payload);
        return ResponseEntity.ok(Map.of("message", "Tạo mẫu thông báo thành công"));
    }

    public ResponseEntity<Map<String, Object>> updateNotificationTemplate(
            Integer id,
            AdminNotificationTemplateUpdateRequest payload,
            Long actorId,
            HttpServletRequest request
    ) {
        boolean active = payload.getActive() == null || payload.getActive();
        jdbcTemplate.update(
                "UPDATE notification_templates SET title = ?, content = ?, channel = ?, is_active = ?, active = ?, updated_at = NOW() WHERE id = ?",
                payload.getTitle().trim(),
                payload.getContent().trim(),
                payload.getChannel().trim().toUpperCase(),
                active ? 1 : 0,
                active ? 1 : 0,
                id
        );
        auditLogService.writeAudit(actorId, "UPDATE_NOTIFICATION_TEMPLATE", "NOTIFICATION_TEMPLATE", id.longValue(), "SUCCESS", "Cập nhật mẫu thông báo", String.valueOf(id), request, null, payload);
        return ResponseEntity.ok(Map.of("message", "Cập nhật mẫu thông báo thành công"));
    }

    public ResponseEntity<Map<String, Object>> toggleNotificationTemplateActive(Integer id) {
        jdbcTemplate.update(
                "UPDATE notification_templates SET is_active = IF(COALESCE(is_active,0)=1 OR COALESCE(active,0)=1, 0, 1), active = IF(COALESCE(is_active,0)=1 OR COALESCE(active,0)=1, 0, 1), updated_at = NOW() WHERE id = ?",
                id
        );
        return ResponseEntity.ok(Map.of("message", "Đã cập nhật trạng thái"));
    }

    public ResponseEntity<Map<String, Object>> deleteNotificationTemplate(Integer id) {
        jdbcTemplate.update("DELETE FROM notification_templates WHERE id = ?", id);
        return ResponseEntity.ok(Map.of("message", "Xóa mẫu thông báo thành công"));
    }

    private String valueOrDefault(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim();
    }
}
