package com.swp391.rescuemanagement.dto.response;

import com.swp391.rescuemanagement.model.Team;

public record TeamResponse(
        Long id,
        String code,
        String name,
        String teamType,
        String status) {
    public static TeamResponse from(Team t) {
        return new TeamResponse(
                t.getId(),
                t.getCode(),
                t.getName(),
                t.getTeamType(),
                t.getStatus());
    }
}
