package com.floodrescue.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorData(String message, Object errors) {

    public static ApiErrorData of(String message) {
        return new ApiErrorData(message, null);
    }

    public static ApiErrorData of(String message, Object errors) {
        return new ApiErrorData(message, errors);
    }
}