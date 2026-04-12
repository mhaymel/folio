package com.folio.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.util.List;

public final class DashboardDto {
    private DashboardSummary summary;
    private DashboardLists lists;
    private String lastQuoteFetchAt;

    public DashboardDto() {
        this.summary = new DashboardSummary(null, null, null);
        this.lists = new DashboardLists(null, null);
    }

    public DashboardDto(DashboardSummary summary, DashboardLists lists, String lastQuoteFetchAt) {
        this.summary = summary;
        this.lists = lists;
        this.lastQuoteFetchAt = lastQuoteFetchAt;
    }

    @JsonUnwrapped
    public DashboardSummary getSummary() { return summary; }
    @JsonUnwrapped
    public DashboardLists getLists() { return lists; }

    public Double getTotalPortfolioValue() { return summary.totalPortfolioValue(); }
    public void setTotalPortfolioValue(Double totalPortfolioValue) { this.summary = new DashboardSummary(totalPortfolioValue, summary.stockCount(), summary.totalDividendRatio()); }
    public Integer getStockCount() { return summary.stockCount(); }
    public void setStockCount(Integer stockCount) { this.summary = new DashboardSummary(summary.totalPortfolioValue(), stockCount, summary.totalDividendRatio()); }
    public Double getTotalDividendRatio() { return summary.totalDividendRatio(); }
    public void setTotalDividendRatio(Double totalDividendRatio) { this.summary = new DashboardSummary(summary.totalPortfolioValue(), summary.stockCount(), totalDividendRatio); }
    public List<HoldingDto> getTop5Holdings() { return lists.top5Holdings(); }
    public void setTop5Holdings(List<HoldingDto> top5Holdings) { this.lists = new DashboardLists(top5Holdings, lists.top5DividendSources()); }
    public List<DividendSourceDto> getTop5DividendSources() { return lists.top5DividendSources(); }
    public void setTop5DividendSources(List<DividendSourceDto> top5DividendSources) { this.lists = new DashboardLists(lists.top5Holdings(), top5DividendSources); }
    public String getLastQuoteFetchAt() { return lastQuoteFetchAt; }
    public void setLastQuoteFetchAt(String lastQuoteFetchAt) { this.lastQuoteFetchAt = lastQuoteFetchAt; }

}