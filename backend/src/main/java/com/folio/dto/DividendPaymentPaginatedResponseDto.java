package com.folio.dto;

import java.util.List;

public final class DividendPaymentPaginatedResponseDto {
    private final List<DividendPaymentDto> items;
    private final int page;
    private final int pageSize;
    private final long totalItems;
    private final int totalPages;
    private final Double sumValue;

    public DividendPaymentPaginatedResponseDto(List<DividendPaymentDto> items, int page, int pageSize,
                                                long totalItems, int totalPages, Double sumValue) {
        this.items = items;
        this.page = page;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
        this.totalPages = totalPages;
        this.sumValue = sumValue;
    }

    public List<DividendPaymentDto> getItems() { return items; }
    public int getPage() { return page; }
    public int getPageSize() { return pageSize; }
    public long getTotalItems() { return totalItems; }
    public int getTotalPages() { return totalPages; }
    public Double getSumValue() { return sumValue; }
}