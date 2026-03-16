package com.floodrescue.module.admin.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminDashboardStatsResponse {
    private long totalUsers;
    private long activeUsers;
    private long lockedUsers;
}
