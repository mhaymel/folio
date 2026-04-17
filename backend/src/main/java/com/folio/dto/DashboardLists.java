package com.folio.dto;

import java.util.List;

import static java.util.Objects.requireNonNull;

public record DashboardLists(List<HoldingDto> top5Holdings, List<DividendSourceDto> top5DividendSources) {
    public DashboardLists {
        top5Holdings = List.copyOf(requireNonNull(top5Holdings));
        top5DividendSources = List.copyOf(requireNonNull(top5DividendSources));
    }
}
