package com.floodrescue.module.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final JdbcTemplate jdbcTemplate;

    public Map<String, Object> getStats() {
        Long total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class);
        Long active = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE status = 1", Long.class);
        Long locked = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE status <> 1", Long.class);

        Map<String, Object> data = new HashMap<>();
        data.put("totalUsers", total == null ? 0 : total);
        data.put("activeUsers", active == null ? 0 : active);
        data.put("lockedUsers", locked == null ? 0 : locked);
        return data;
    }
}
