package com.floodrescue.module.notification.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OverloadEmergencyRequest {
    @NotNull(message = "queueRequestId không được để trống")
    @Positive(message = "queueRequestId phải lớn hơn 0")
    private Long queueRequestId;

    @Size(max = 2000, message = "Ghi chú không được vượt quá 2000 ký tự")
    private String note;
}
