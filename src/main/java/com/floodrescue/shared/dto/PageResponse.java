package com.floodrescue.shared.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Lightweight page wrapper for FE contracts that expect
 * content/totalElements/number/size without the ApiResult envelope.
 */
public record PageResponse<T>(
        List<T> content,
        long totalElements,
        int number,
        int size
) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getTotalElements(),
                page.getNumber(),
                page.getSize()
        );
    }
}

