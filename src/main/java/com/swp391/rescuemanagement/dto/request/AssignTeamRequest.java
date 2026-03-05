package com.swp391.rescuemanagement.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * DTO gán task_group vào rescue_request
 */
public record AssignTeamRequest(

        @NotNull(message = "rescueRequestId không được để trống") Long rescueRequestId,

        @NotNull(message = "taskGroupId không được để trống") Long taskGroupId,

        Long assetId // optional
) {
}
