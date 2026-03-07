package com.floodrescue.shared.enums;

/**
 * Vehicle types for rescue and relief operations
 * Organized by category for flood rescue coordination
 */
public enum VehicleType {
    // Rescue Vehicles (Phương tiện cứu người)
    MOTORBOAT("Xuồng máy / Cano cứu hộ", "RESCUE"),
    INFLATABLE_BOAT("Thuyền cao su", "RESCUE"),
    AMBULANCE("Xe cứu thương", "RESCUE"),
    RESCUE_TRUCK("Xe cứu hộ đa năng", "RESCUE"),
    RESCUE_HELICOPTER("Trực thăng cứu hộ", "RESCUE"),

    // Relief Supply Transport (Phương tiện vận chuyển hàng cứu trợ)
    CARGO_TRUCK("Xe tải cứu trợ", "SUPPLY"),
    PICKUP_TRUCK("Xe bán tải", "SUPPLY"),
    MOBILE_CONTAINER("Container di động", "SUPPLY"),

    // Technical Support (Phương tiện hỗ trợ kỹ thuật)
    EXCAVATOR("Xe múc / Xe xúc", "TECHNICAL"),
    WATER_PUMP("Máy bơm nước công suất lớn", "TECHNICAL"),
    GENERATOR("Máy phát điện lưu động", "TECHNICAL"),
    COMMAND_CENTER("Xe chỉ huy lưu động", "TECHNICAL"),

    // Small Equipment (Phương tiện nhỏ / thiết bị đi kèm)
    LIFE_VEST("Áo phao", "EQUIPMENT"),
    LIFE_BUOY("Phao cứu sinh", "EQUIPMENT"),
    RADIO("Bộ đàm", "EQUIPMENT"),
    DRONE("Drone", "EQUIPMENT");

    private final String vietnameseName;
    private final String category;

    VehicleType(String vietnameseName, String category) {
        this.vietnameseName = vietnameseName;
        this.category = category;
    }

    public String getVietnameseName() {
        return vietnameseName;
    }

    public String getCategory() {
        return category;
    }
}
