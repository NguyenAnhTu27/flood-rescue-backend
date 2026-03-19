package com.floodrescue.module.relief.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReliefRequestLineRequest {

    @NotNull(message = "ID loại hàng không được để trống")
    @Positive(message = "ID loại hàng phải lớn hơn 0")
    private Integer itemCategoryId;

    @NotNull(message = "Số lượng không được để trống")
    @Positive(message = "Số lượng phải lớn hơn 0")
    private Double qty;

    @NotBlank(message = "Đơn vị tính không được để trống")
    @Size(max = 50, message = "Đơn vị tính không được vượt quá 50 ký tự")
    private String unit;
}

