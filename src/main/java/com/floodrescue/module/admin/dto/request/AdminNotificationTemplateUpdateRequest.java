package com.floodrescue.module.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminNotificationTemplateUpdateRequest {

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 255, message = "Tiêu đề không được vượt quá 255 ký tự")
    private String title;

    @NotBlank(message = "Nội dung không được để trống")
    @Size(max = 5000, message = "Nội dung không được vượt quá 5000 ký tự")
    private String content;

    @NotBlank(message = "Kênh thông báo không được để trống")
    @Size(max = 30, message = "Kênh thông báo không được vượt quá 30 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_\\-]+$", message = "Kênh thông báo không hợp lệ")
    private String channel;

    private Boolean active;
}
