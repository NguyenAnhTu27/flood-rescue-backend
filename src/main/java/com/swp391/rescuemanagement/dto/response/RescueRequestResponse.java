package com.swp391.rescuemanagement.dto.response;

import com.swp391.rescuemanagement.model.RescueRequest;
import java.time.LocalDateTime;

public record RescueRequestResponse(
        Long id,
        String code,
        String status,
        String priority,
        String addressText,
        String description,
        Integer affectedPeopleCount,
        Boolean locationVerified,
        Long callerId,
        String callerName,
        String callerEmail,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
    public static RescueRequestResponse from(RescueRequest r) {
        return new RescueRequestResponse(
                r.getId(),
                r.getCode(),
                r.getStatus(),
                r.getPriority(),
                r.getAddressText(),
                r.getDescription(),
                r.getAffectedPeopleCount(),
                r.getLocationVerified(),
                r.getCaller() != null ? r.getCaller().getId() : null,
                r.getCaller() != null ? r.getCaller().getFullName() : null,
                r.getCaller() != null ? r.getCaller().getEmail() : null,
                r.getCreatedAt(),
                r.getUpdatedAt());
    }
}
