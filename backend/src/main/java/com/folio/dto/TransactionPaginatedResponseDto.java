package com.folio.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.util.List;

import static java.util.Objects.requireNonNull;

public final class TransactionPaginatedResponseDto {
    private final List<TransactionDto> items;
    private final PaginationData pagination;
    private final TransactionAggregates aggregates;

    public TransactionPaginatedResponseDto(List<TransactionDto> items, int page, int pageSize,
                                           long totalItems, int totalPages,
                                           long filteredCount, Double sumCount) {
        this.items = requireNonNull(items);
        this.pagination = new PaginationData(page, pageSize, totalItems, totalPages);
        this.aggregates = new TransactionAggregates(filteredCount, sumCount);
    }

    public List<TransactionDto> getItems() { return items; }
    @JsonUnwrapped
    public PaginationData getPagination() { return pagination; }
    public int getPage() { return pagination.page(); }
    public int getPageSize() { return pagination.pageSize(); }
    public long getTotalItems() { return pagination.totalItems(); }
    public int getTotalPages() { return pagination.totalPages(); }
    @JsonUnwrapped
    public TransactionAggregates getAggregates() { return aggregates; }
    public long getFilteredCount() { return aggregates.filteredCount(); }
    public Double getSumCount() { return aggregates.sumCount(); }
}