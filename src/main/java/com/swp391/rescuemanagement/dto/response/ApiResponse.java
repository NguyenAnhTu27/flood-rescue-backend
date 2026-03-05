package com.swp391.rescuemanagement.dto.response;

/**
 * Generic API response wrapper — dùng Java 21 Record
 *
 * Thành công: { "success": true, "message": "...", "data": {...} }
 * Lỗi: { "success": false, "message": "...", "data": null }
 */
public record ApiResponse<T>(
        boolean success,
        String message,
        T data) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "Thành công", data);
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
