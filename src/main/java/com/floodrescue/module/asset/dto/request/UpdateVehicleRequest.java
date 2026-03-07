package com.floodrescue.module.vehicle.dto.request;

import java.time.LocalDateTime;

import com.floodrescue.shared.enums.VehicleStatus;
import com.floodrescue.shared.enums.VehicleType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateVehicleRequest {

    @NotBlank(message = "Tên phương tiện không được để trống")
    @Size(min = 5, max = 120, message = "Tên phương tiện phải từ 5-120 ký tự")
    private String name;

    @NotNull(message = "Loại phương tiện không được để trống")
    private VehicleType vehicleType;

    @NotNull(message = "Trạng thái không được để trống")
    private VehicleStatus status;

    @Positive(message = "Sức chứa phải là số dương")
    private Integer capacity;

    @Size(max = 255, message = "Vị trí không được vượt quá 255 ký tự")
    private String location;

    @Size(max = 20, message = "Biển số không được vượt quá 20 ký tự")
    private String licensePlate;

    @Size(max = 50, message = "VIN không được vượt quá 50 ký tự")
    private String vinNumber;

    private LocalDateTime lastMaintenanceDate;

    private LocalDateTime nextMaintenanceDate;

    private String description;

    @Size(max = 20, message = "Số điện thoại liên lạc không được vượt quá 20 ký tự")
    private String contactNumber;

    private Long assignedTeamId;
}
