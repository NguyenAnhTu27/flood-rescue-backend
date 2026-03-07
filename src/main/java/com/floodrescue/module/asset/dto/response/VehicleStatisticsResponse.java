package com.floodrescue.module.vehicle.dto.response;

import java.util.List;

import com.floodrescue.shared.enums.VehicleStatus;
import com.floodrescue.shared.enums.VehicleType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleStatisticsResponse {
    private long totalVehicles;
    private long availableVehicles;
    private long inUseVehicles;
    private long maintenanceVehicles;
    private List<VehicleTypeCount> byType;
    private List<VehicleStatusCount> byStatus;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VehicleTypeCount {
        private VehicleType type;
        private String typeName;
        private long count;
        private long availableCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VehicleStatusCount {
        private VehicleStatus status;
        private long count;
    }
}
