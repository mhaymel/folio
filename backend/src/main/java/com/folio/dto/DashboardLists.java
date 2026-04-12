package com.folio.dto;

import java.util.List;

/**
 * Groups top-N list data for the dashboard.
 */
public record DashboardLists(List<HoldingDto> top5Holdings, List<DividendSourceDto> top5DividendSources) {
}

