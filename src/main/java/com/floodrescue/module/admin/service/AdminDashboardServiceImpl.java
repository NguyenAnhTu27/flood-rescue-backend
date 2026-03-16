package com.floodrescue.module.admin.service;

import com.floodrescue.module.admin.dto.AdminDashboardStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public AdminDashboardStatsResponse getStats() {
        Long total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class);
        Long active = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE status = 1", Long.class);
        Long locked = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE status <> 1", Long.class);

        return AdminDashboardStatsResponse.builder()
                .totalUsers(total == null ? 0 : total)
                .activeUsers(active == null ? 0 : active)
                .lockedUsers(locked == null ? 0 : locked)
                .build();
    }
}
