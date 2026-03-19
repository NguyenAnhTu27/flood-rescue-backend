package com.floodrescue.module.rescue.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTeamLocationRequest {

    @NotNull(message = "Vĩ độ không được để trống")
    @DecimalMin(value = "-90.0", message = "Vĩ độ phải lớn hơn hoặc bằng -90")
    @DecimalMax(value = "90.0", message = "Vĩ độ phải nhỏ hơn hoặc bằng 90")
    private Double latitude;

    @NotNull(message = "Kinh độ không được để trống")
    @DecimalMin(value = "-180.0", message = "Kinh độ phải lớn hơn hoặc bằng -180")
    @DecimalMax(value = "180.0", message = "Kinh độ phải nhỏ hơn hoặc bằng 180")
    private Double longitude;

    @Size(max = 255, message = "Mô tả vị trí không được vượt quá 255 ký tự")
    private String locationText;
}
