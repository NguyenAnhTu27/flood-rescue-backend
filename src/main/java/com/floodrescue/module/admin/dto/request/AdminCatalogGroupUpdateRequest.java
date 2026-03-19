package com.floodrescue.module.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminCatalogGroupUpdateRequest {

    @NotBlank(message = "Tên nhóm không được để trống")
    @Size(max = 255, message = "Tên nhóm không được vượt quá 255 ký tự")
    private String name;
}
