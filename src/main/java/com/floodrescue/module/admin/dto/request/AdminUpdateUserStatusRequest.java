package com.floodrescue.module.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUpdateUserStatusRequest {

    @NotBlank(message = "Trạng thái không được để trống")
    @Pattern(regexp = "(?i)^(ACTIVE|LOCKED)$", message = "Trạng thái chỉ hỗ trợ ACTIVE hoặc LOCKED")
    private String status;
}
