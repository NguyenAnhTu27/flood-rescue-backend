package com.floodrescue.module.rescue.dto.request;

import com.floodrescue.shared.enums.RescuePriority;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RescueRequestUpdateRequest {

    @Min(value = 1, message = "Số người bị ảnh hưởng phải lớn hơn 0")
    private Integer affectedPeopleCount;

    @Size(max = 2000, message = "Mô tả không được vượt quá 2000 ký tự")
    private String description;

    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    private String addressText;

    private RescuePriority priority;
}
