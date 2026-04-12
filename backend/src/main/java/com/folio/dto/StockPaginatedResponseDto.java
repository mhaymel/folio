package com.folio.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.util.List;

import static java.util.Objects.requireNonNull;

public final class StockPaginatedResponseDto {
    private final List<StockDto> items;
    private final PaginationData pagination;
    private final Double sumCount;

    public StockPaginatedResponseDto(List<StockDto> items, int page, int pageSize,
                                     long totalItems, int totalPages, Double sumCount) {
        this.items = requireNonNull(items);
        this.pagination = new PaginationData(page, pageSize, totalItems, totalPages);
        this.sumCount = sumCount;
    }

    public List<StockDto> getItems() { return items; }
    @JsonUnwrapped
    public PaginationData getPagination() { return pagination; }
    public int getPage() { return pagination.page(); }
    public int getPageSize() { return pagination.pageSize(); }
    public long getTotalItems() { return pagination.totalItems(); }
    public int getTotalPages() { return pagination.totalPages(); }
    public Double getSumCount() { return sumCount; }
}
