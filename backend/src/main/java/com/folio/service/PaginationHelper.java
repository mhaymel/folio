package com.folio.service;

import com.folio.dto.PaginatedResponseDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public final class PaginationHelper {

    private static final List<Integer> VALID_PAGE_SIZES = List.of(10, 20, 50, 100, -1);

    public <T> PaginatedResponseDto<T> paginate(List<T> items, int page, int pageSize) {
        int effectivePageSize = VALID_PAGE_SIZES.contains(pageSize) ? pageSize : 10;

        long totalItems = items.size();

        if (effectivePageSize == -1) {
            return new PaginatedResponseDto<>(items, 1, -1, totalItems, 1);
        }

        int effectivePage = Math.max(page, 1);

        int totalPages = (int) Math.ceil((double) totalItems / effectivePageSize);
        if (totalPages == 0) totalPages = 1;

        int fromIndex = (effectivePage - 1) * effectivePageSize;
        if (fromIndex >= items.size()) {
            return new PaginatedResponseDto<>(List.of(), effectivePage, effectivePageSize, totalItems, totalPages);
        }
        int toIndex = Math.min(fromIndex + effectivePageSize, items.size());
        List<T> pageItems = items.subList(fromIndex, toIndex);

        return new PaginatedResponseDto<>(pageItems, effectivePage, effectivePageSize, totalItems, totalPages);
    }
}