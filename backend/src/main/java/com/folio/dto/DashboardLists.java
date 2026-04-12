package com.folio.dto;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Groups top-N list data for the dashboard.
 */
public record DashboardLists(List<HoldingDto> top5Holdings, List<DividendSourceDto> top5DividendSources) {
    public DashboardLists {
        requireNonNull(top5Holdings);
        requireNonNull(top5DividendSources);
        top5Holdings = List.copyOf(top5Holdings);
        top5DividendSources = List.copyOf(top5DividendSources);
    }
}

