package com.folio.dto;

import java.time.LocalDateTime;
import java.util.List;

public final class DashboardDtoBuilder {
    private Double totalPortfolioValue;
    private Integer stockCount;
    private Double totalDividendRatio;
    private List<HoldingDto> top5Holdings;
    private List<DividendSourceDto> top5DividendSources;
    private LocalDateTime lastQuoteFetchAt;

    public DashboardDtoBuilder totalPortfolioValue(Double v) { this.totalPortfolioValue = v; return this; }
    public DashboardDtoBuilder stockCount(Integer v) { this.stockCount = v; return this; }
    public DashboardDtoBuilder totalDividendRatio(Double v) { this.totalDividendRatio = v; return this; }
    public DashboardDtoBuilder top5Holdings(List<HoldingDto> v) { this.top5Holdings = v; return this; }
    public DashboardDtoBuilder top5DividendSources(List<DividendSourceDto> v) { this.top5DividendSources = v; return this; }
    public DashboardDtoBuilder lastQuoteFetchAt(LocalDateTime v) { this.lastQuoteFetchAt = v; return this; }

    public DashboardDto build() {
        DashboardDto d = new DashboardDto();
        d.setTotalPortfolioValue(totalPortfolioValue);
        d.setStockCount(stockCount);
        d.setTotalDividendRatio(totalDividendRatio);
        d.setTop5Holdings(top5Holdings);
        d.setTop5DividendSources(top5DividendSources);
        d.setLastQuoteFetchAt(lastQuoteFetchAt);
        return d;
    }
}

