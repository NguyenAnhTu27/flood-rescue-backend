package com.floodrescue.module.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationTemplateService {

    private final JdbcTemplate jdbcTemplate;

    @Transactional(readOnly = true)
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

        List<Object> queryParams = new ArrayList<>(params);
        queryParams.add(safeSize);
        queryParams.add(offset);

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
                queryParams.toArray()
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

    @Transactional
    public String createNotificationTemplate(Map<String, Object> payload) {
        String code = String.valueOf(payload.getOrDefault("code", "")).trim().toUpperCase();
        String templateKey = String.valueOf(payload.getOrDefault("templateKey", code)).trim().toUpperCase();
        String title = String.valueOf(payload.getOrDefault("title", "")).trim();
        String channel = String.valueOf(payload.getOrDefault("channel", "WEB")).trim().toUpperCase();
        String content = String.valueOf(payload.getOrDefault("content", "")).trim();
        boolean active = Boolean.parseBoolean(String.valueOf(payload.getOrDefault("active", "true")));

        jdbcTemplate.update(
                "INSERT INTO notification_templates(code, template_key, title, content, channel, is_active, active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())",
                code, templateKey, title, content, channel, active ? 1 : 0, active ? 1 : 0
        );
        return code;
    }

    @Transactional
    public void updateNotificationTemplate(Integer id, Map<String, Object> payload) {
        jdbcTemplate.update(
                "UPDATE notification_templates SET title = ?, content = ?, channel = ?, is_active = ?, active = ?, updated_at = NOW() WHERE id = ?",
                String.valueOf(payload.getOrDefault("title", "")).trim(),
                String.valueOf(payload.getOrDefault("content", "")).trim(),
                String.valueOf(payload.getOrDefault("channel", "WEB")).trim().toUpperCase(),
                Boolean.parseBoolean(String.valueOf(payload.getOrDefault("active", "true"))) ? 1 : 0,
                Boolean.parseBoolean(String.valueOf(payload.getOrDefault("active", "true"))) ? 1 : 0,
                id
        );
    }

    @Transactional
    public void toggleNotificationTemplateActive(Integer id) {
        jdbcTemplate.update(
                "UPDATE notification_templates SET is_active = IF(COALESCE(is_active,0)=1 OR COALESCE(active,0)=1, 0, 1), active = IF(COALESCE(is_active,0)=1 OR COALESCE(active,0)=1, 0, 1), updated_at = NOW() WHERE id = ?",
                id
        );
    }

    @Transactional
    public void deleteNotificationTemplate(Integer id) {
        jdbcTemplate.update("DELETE FROM notification_templates WHERE id = ?", id);
    }
}