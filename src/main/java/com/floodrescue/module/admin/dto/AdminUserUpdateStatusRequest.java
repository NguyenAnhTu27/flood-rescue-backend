package com.floodrescue.module.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserUpdateStatusRequest {

    @NotBlank(message = "Trạng thái không được để trống")
    private String status;
}

