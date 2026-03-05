package com.swp391.rescuemanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO cập nhật status rescue_request hoặc assignment
 */
public record UpdateStatusRequest(

        @NotBlank(message = "Status không được để trống") @Pattern(regexp = "in_progress|completed|cancelled", message = "Status phải là: in_progress, completed, cancelled") String status) {
}
