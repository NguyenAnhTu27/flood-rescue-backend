package com.floodrescue.module.admin.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminCatalogRequest {

    @NotBlank(message = "groupCode không được để trống")
    @Size(max = 50, message = "groupCode không được vượt quá 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_\\-]+$", message = "groupCode không hợp lệ")
    private String groupCode;

    @NotBlank(message = "code không được để trống")
    @Size(max = 50, message = "code không được vượt quá 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_\\-]+$", message = "code không hợp lệ")
    private String code;

    @Size(max = 255, message = "Tên không được vượt quá 255 ký tự")
    private String name;

    @Size(max = 255, message = "Tên tiếng Việt không được vượt quá 255 ký tự")
    private String nameVn;

    private Boolean active;

    @AssertTrue(message = "Tên danh mục không được để trống")
    public boolean isNameProvided() {
        return hasText(name) || hasText(nameVn);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
