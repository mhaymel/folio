package com.folio.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import static com.folio.precondition.Precondition.notNull;

public final class DashboardDto {
    private DashboardSummary summary;
    private DashboardLists lists;
    private String lastQuoteFetchAt;

    public DashboardDto(DashboardSummary summary, DashboardLists lists, String lastQuoteFetchAt) {
        this.summary = notNull(summary);
        this.lists = notNull(lists);
        this.lastQuoteFetchAt = notNull(lastQuoteFetchAt);
    }

    @JsonUnwrapped
    public DashboardSummary getSummary() { return summary; }
    @JsonUnwrapped
    public DashboardLists getLists() { return lists; }
    public String getLastQuoteFetchAt() { return lastQuoteFetchAt; }
}