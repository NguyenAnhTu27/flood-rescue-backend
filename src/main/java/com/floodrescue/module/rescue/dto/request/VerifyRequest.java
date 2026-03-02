package com.floodrescue.module.rescue.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyRequest {

    @NotNull(message = "Trạng thái xác minh không được để trống")
    private Boolean locationVerified;

    private String note;
}
