package com.folio.service;

import com.folio.dto.PaginatedResponseDto;

import java.util.List;

public final class PaginationHelper {

    private static final List<Integer> VALID_PAGE_SIZES = List.of(10, 20, 50, 100, -1);

    private PaginationHelper() {}

    public static <T> PaginatedResponseDto<T> paginate(List<T> items, int page, int pageSize) {
        if (!VALID_PAGE_SIZES.contains(pageSize)) {
            pageSize = 10;
        }

        long totalItems = items.size();

        if (pageSize == -1) {
            return new PaginatedResponseDto<>(items, 1, -1, totalItems, 1);
        }

        if (page < 1) page = 1;

        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        if (totalPages == 0) totalPages = 1;

        int fromIndex = (page - 1) * pageSize;
        if (fromIndex >= items.size()) {
            return new PaginatedResponseDto<>(List.of(), page, pageSize, totalItems, totalPages);
        }
        int toIndex = Math.min(fromIndex + pageSize, items.size());
        List<T> pageItems = items.subList(fromIndex, toIndex);

        return new PaginatedResponseDto<>(pageItems, page, pageSize, totalItems, totalPages);
    }
}