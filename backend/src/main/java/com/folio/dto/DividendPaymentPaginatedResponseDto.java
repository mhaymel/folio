package com.folio.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.util.List;

import static java.util.Objects.requireNonNull;

public final class DividendPaymentPaginatedResponseDto {
    private final List<DividendPaymentDto> items;
    private final PaginationData pagination;
    private final Double sumValue;

    public DividendPaymentPaginatedResponseDto(List<DividendPaymentDto> items, int page, int pageSize,
                                               long totalItems, int totalPages, Double sumValue) {
        this.items = requireNonNull(items);
        this.pagination = new PaginationData(page, pageSize, totalItems, totalPages);
        this.sumValue = sumValue;
    }

    public List<DividendPaymentDto> getItems() { return items; }
    @JsonUnwrapped
    public PaginationData getPagination() { return pagination; }
    public int getPage() { return pagination.page(); }
    public int getPageSize() { return pagination.pageSize(); }
    public long getTotalItems() { return pagination.totalItems(); }
    public int getTotalPages() { return pagination.totalPages(); }
    public Double getSumValue() { return sumValue; }
}