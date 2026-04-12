package com.folio.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.util.List;

import static java.util.Objects.requireNonNull;

public final class PaginatedResponseDto<T> {
    private final List<T> items;
    private final PaginationData pagination;

    public PaginatedResponseDto(List<T> items, int page, int pageSize, long totalItems, int totalPages) {
        this.items = requireNonNull(items);
        this.pagination = new PaginationData(page, pageSize, totalItems, totalPages);
    }

    public List<T> getItems() { return items; }
    @JsonUnwrapped
    public PaginationData getPagination() { return pagination; }
    public int getPage() { return pagination.page(); }
    public int getPageSize() { return pagination.pageSize(); }
    public long getTotalItems() { return pagination.totalItems(); }
    public int getTotalPages() { return pagination.totalPages(); }
}