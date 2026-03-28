package com.folio.dto;

import java.util.List;

public final class TransactionPaginatedResponseDto {
    private final List<TransactionDto> items;
    private final int page;
    private final int pageSize;
    private final long totalItems;
    private final int totalPages;
    private final long filteredCount;
    private final Double sumCount;

    public TransactionPaginatedResponseDto(List<TransactionDto> items, int page, int pageSize,
                                           long totalItems, int totalPages,
                                           long filteredCount, Double sumCount) {
        this.items = items;
        this.page = page;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
        this.totalPages = totalPages;
        this.filteredCount = filteredCount;
        this.sumCount = sumCount;
    }

    public List<TransactionDto> getItems() { return items; }
    public int getPage() { return page; }
    public int getPageSize() { return pageSize; }
    public long getTotalItems() { return totalItems; }
    public int getTotalPages() { return totalPages; }
    public long getFilteredCount() { return filteredCount; }
    public Double getSumCount() { return sumCount; }
}