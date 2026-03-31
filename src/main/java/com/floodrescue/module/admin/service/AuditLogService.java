package com.floodrescue.module.admin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public String getActorName(Long userId) {
        if (userId == null) {
            return "SYSTEM";
        }
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT full_name FROM users WHERE id = ?",
                    String.class,
                    userId
            );
        } catch (EmptyResultDataAccessException ex) {
            return "SYSTEM";
        }
    }

    public void writeAudit(
            Long actorId,
            String action,
            String entityType,
            Long entityId,
            String level,
            String detail,
            String target,
            HttpServletRequest request,
            Object oldData,
            Object newData
    ) {
        if (actorId == null) {
            return;
        }

        String actor = getActorName(actorId);
        String ip = request != null ? request.getRemoteAddr() : null;
        String ua = request != null ? request.getHeader("User-Agent") : null;
        String oldJson = toJson(oldData);
        String newJson = toJson(newData);

        jdbcTemplate.update(
                "INSERT INTO audit_logs(actor_id, action, entity_type, entity_id, old_data, new_data, ip_address, user_agent, created_at, actor, detail, level, target) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), ?, ?, ?, ?)",
                actorId, action, entityType, entityId, oldJson, newJson, ip, ua, actor, detail, level, target
        );
    }

    public Map<String, Object> getAuditLogs(int page, int size, String action, String keyword) {
        int safeSize = Math.max(1, Math.min(size, 200));
        int safePage = Math.max(0, page);
        int offset = safePage * safeSize;

        StringBuilder where = new StringBuilder(" WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (action != null && !action.isBlank()) {
            where.append(" AND action = ? ");
            params.add(action);
        }
        if (keyword != null && !keyword.isBlank()) {
            where.append(" AND (actor LIKE ? OR target LIKE ? OR action LIKE ?) ");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }

        List<Object> queryParams = new ArrayList<>(params);
        queryParams.add(safeSize);
        queryParams.add(offset);
        List<Map<String, Object>> items = jdbcTemplate.query(
                "SELECT id, created_at, action, actor, target, level, detail FROM audit_logs " +
                        where + " ORDER BY id DESC LIMIT ? OFFSET ?",
                (rs, rowNum) -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", rs.getLong("id"));
                    item.put("createdAt", rs.getTimestamp("created_at"));
                    item.put("action", rs.getString("action"));
                    item.put("actor", rs.getString("actor"));
                    item.put("target", rs.getString("target"));
                    item.put("level", rs.getString("level"));
                    item.put("detail", rs.getString("detail"));
                    return item;
                },
                queryParams.toArray()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("items", items);
        response.put("page", safePage);
        response.put("size", safeSize);
        return response;
    }

    private String toJson(Object data) {
        if (data == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
