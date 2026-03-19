package com.floodrescue.module.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AdminUpdateRolePermissionsRequest {

    private List<@NotBlank(message = "Mã quyền không được để trống")
            @Size(max = 100, message = "Mã quyền không được vượt quá 100 ký tự") String> permissions;
}
