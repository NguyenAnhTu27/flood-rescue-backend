package com.floodrescue.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResult<T>(boolean success, String message, T data) {

    /* ---- success helpers ---- */

    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<>(true, null, data);
    }

    public static ApiResult<Void> ok(String message) {
        return new ApiResult<>(true, message, null);
    }

    public static <T> ApiResult<T> ok(String message, T data) {
        return new ApiResult<>(true, message, data);
    }

    /* ---- error helpers ---- */

    @SuppressWarnings("unchecked")
    public static <T> ApiResult<T> error(String message) {
        return new ApiResult<>(false, message, (T) ApiErrorData.of(message));
    }

    @SuppressWarnings("unchecked")
    public static <T> ApiResult<T> error(String message, Object errors) {
        return new ApiResult<>(false, message, (T) ApiErrorData.of(message, errors));
    }
}
