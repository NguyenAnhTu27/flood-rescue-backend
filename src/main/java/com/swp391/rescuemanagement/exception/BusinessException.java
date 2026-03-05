package com.swp391.rescuemanagement.exception;

import lombok.Getter;

/**
 * Exception cho lỗi nghiệp vụ (business logic).
 * Mang theo HTTP status code để GlobalExceptionHandler trả về đúng status.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int httpStatus;

    public BusinessException(String message, int httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    // Factory methods cho các case phổ biến
    public static BusinessException notFound(String entity, Long id) {
        return new BusinessException("Không tìm thấy " + entity + " với id=" + id, 404);
    }

    public static BusinessException conflict(String message) {
        return new BusinessException(message, 409);
    }

    public static BusinessException badRequest(String message) {
        return new BusinessException(message, 400);
    }

    public static BusinessException unauthorized(String message) {
        return new BusinessException(message, 401);
    }
}
