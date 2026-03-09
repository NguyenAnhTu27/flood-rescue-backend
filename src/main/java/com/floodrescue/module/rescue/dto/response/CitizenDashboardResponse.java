package com.floodrescue.module.rescue.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CitizenDashboardResponse {
    private long totalRequests;
    private long pendingRequests;
    private long inProgressRequests;
    private long completedRequests;
    private long cancelledRequests;
    private long duplicateRequests;

    private RescueRequestResponse latestRequest;
    private List<RescueRequestResponse> recentRequests;
}
