package com.floodrescue.shared.enums;

/**
 * Vehicle status for fleet management
 */
public enum VehicleStatus {
    AVAILABLE("Có sẵn"),
    IN_USE("Đang sử dụng"),
    MAINTENANCE("Bảo dưỡng"),
    BROKEN("Hỏng hóc"),
    INACTIVE("Không hoạt động");

    private final String displayName;

    VehicleStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
