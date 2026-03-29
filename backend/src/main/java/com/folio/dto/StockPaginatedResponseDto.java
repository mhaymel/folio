package com.folio.dto;

import java.util.List;

public final class StockPaginatedResponseDto {
    private final List<StockDto> items;
    private final int page;
    private final int pageSize;
    private final long totalItems;
    private final int totalPages;
    private final Double sumCount;

    public StockPaginatedResponseDto(List<StockDto> items, int page, int pageSize,
                                     long totalItems, int totalPages, Double sumCount) {
        this.items = items;
        this.page = page;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
        this.totalPages = totalPages;
        this.sumCount = sumCount;
    }

    public List<StockDto> getItems() { return items; }
    public int getPage() { return page; }
    public int getPageSize() { return pageSize; }
    public long getTotalItems() { return totalItems; }
    public int getTotalPages() { return totalPages; }
    public Double getSumCount() { return sumCount; }
}
