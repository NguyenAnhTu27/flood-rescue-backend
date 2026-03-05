package com.floodrescue.module.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemCategoryCreateRequest {

    @NotBlank(message = "Mã loại hàng không được để trống")
    private String code;

    @NotBlank(message = "Tên loại hàng không được để trống")
    private String name;

    @NotBlank(message = "Đơn vị tính không được để trống")
    private String unit;
}

