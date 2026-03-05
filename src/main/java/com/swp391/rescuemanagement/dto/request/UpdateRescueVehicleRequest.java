package com.swp391.rescuemanagement.dto.request;

import jakarta.validation.constraints.*;

/**
 * DTO Request cập nhật RescueVehicle
 */
public record UpdateRescueVehicleRequest(
        @Size(min = 5, max = 100, message = "Name phải từ 5-100 ký tự")
        String name,

        String description,

        @Pattern(regexp = "available|in_use|maintenance|retired",
                message = "Status phải là: available, in_use, maintenance, retired")
        String status,

        @Min(value = 1, message = "Capacity phải >= 1")
        Integer capacity,

        Long dispatcherId,

        Long assignedTeamId,

        @Size(max = 20, message = "License plate tối đa 20 ký tự")
        String licensePlate,

        @Size(max = 20, message = "Contact number tối đa 20 ký tự")
        String contactNumber
) {
}
