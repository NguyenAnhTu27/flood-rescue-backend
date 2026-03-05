package com.swp391.rescuemanagement.dto.request;

import jakarta.validation.constraints.*;

/**
 * DTO Request tạo mới RescueVehicle
 */
public record CreateRescueVehicleRequest(
        @NotBlank(message = "Code không được để trống")
        @Size(min = 3, max = 50, message = "Code phải từ 3-50 ký tự")
        String code,

        @NotBlank(message = "Name không được để trống")
        @Size(min = 5, max = 100, message = "Name phải từ 5-100 ký tự")
        String name,

        @NotBlank(message = "Vehicle type không được để trống")
        @Pattern(regexp = "rescue_person|transport_supplies|technical_support|small_equipment",
                message = "Vehicle type phải là: rescue_person, transport_supplies, technical_support, small_equipment")
        String vehicleType,

        String icon,

        String description,

        @NotBlank(message = "Status không được để trống")
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
