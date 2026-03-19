package com.floodrescue.module.notification.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QueueEmergencyRequest {
    private Boolean direct;

    @Size(max = 2000, message = "Ghi chú không được vượt quá 2000 ký tự")
    private String note;
}
