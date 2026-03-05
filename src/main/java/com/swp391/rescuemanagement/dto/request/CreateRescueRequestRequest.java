package com.swp391.rescuemanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO tạo rescue request mới
 */
public record CreateRescueRequestRequest(

        @NotBlank(message = "Mô tả không được để trống") String description,

        @NotBlank(message = "Địa chỉ không được để trống") String addressText,

        @Pattern(regexp = "low|medium|high|critical", message = "Priority phải là: low, medium, high, critical") String priority,

        Integer affectedPeopleCount) {
    /** Set mặc định priority nếu null */
    public CreateRescueRequestRequest {
        if (priority == null || priority.isBlank())
            priority = "medium";
    }
}
