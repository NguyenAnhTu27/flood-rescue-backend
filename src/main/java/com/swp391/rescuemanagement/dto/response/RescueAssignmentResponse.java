package com.swp391.rescuemanagement.dto.response;

import com.swp391.rescuemanagement.model.RescueAssignment;
import java.time.LocalDateTime;

public record RescueAssignmentResponse(
        Long id,
        Long rescueRequestId,
        String rescueRequestCode,
        Long taskGroupId,
        String taskGroupCode,
        String taskGroupStatus,
        Long teamId,
        String teamName,
        Long assetId,
        String assetName,
        Long assignedById,
        String assignedByName,
        LocalDateTime assignedAt) {
    public static RescueAssignmentResponse from(RescueAssignment a) {
        var tg = a.getTaskGroup();
        var req = tg != null ? null : null; // rescue_request via task_group_requests
        return new RescueAssignmentResponse(
                a.getId(),
                null, // filled by service if needed
                null,
                tg != null ? tg.getId() : null,
                tg != null ? tg.getCode() : null,
                tg != null ? tg.getStatus() : null,
                a.getTeam() != null ? a.getTeam().getId() : null,
                a.getTeam() != null ? a.getTeam().getName() : null,
                a.getAsset() != null ? a.getAsset().getId() : null,
                a.getAsset() != null ? a.getAsset().getName() : null,
                a.getAssignedBy() != null ? a.getAssignedBy().getId() : null,
                a.getAssignedBy() != null ? a.getAssignedBy().getFullName() : null,
                a.getAssignedAt());
    }
}
