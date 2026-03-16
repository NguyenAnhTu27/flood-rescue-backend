package com.floodrescue.shared.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PagedData<T>(List<T> data, long total, int page, int size) {

    public static <T> PagedData<T> from(Page<T> page) {
        return new PagedData<>(
                page.getContent(),
                page.getTotalElements(),
                page.getNumber() + 1,
                page.getSize()
        );
    }

    public static <T> PagedData<T> from(List<T> data) {
        return new PagedData<>(data, data.size(), 1, data.size());
    }
}