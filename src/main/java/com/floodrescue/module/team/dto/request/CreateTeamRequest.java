package com.floodrescue.module.team.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTeamRequest {

    @NotBlank(message = "Tên đội cứu hộ không được để trống")
    private String name;

    private String description;
}

