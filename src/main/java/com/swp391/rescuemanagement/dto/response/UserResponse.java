package com.swp391.rescuemanagement.dto.response;

import com.swp391.rescuemanagement.model.User;

/**
 * Response DTO cho User — Java 21 Record (immutable, no boilerplate)
 */
public record UserResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        String role,
        String status,
        Long teamId,
        String teamName) {
    public static UserResponse from(User u) {
        return new UserResponse(
                u.getId(),
                u.getFullName(),
                u.getEmail(),
                u.getPhone(),
                u.getRole(),
                u.getStatus(),
                u.getTeam() != null ? u.getTeam().getId() : null,
                u.getTeam() != null ? u.getTeam().getName() : null);
    }
}
