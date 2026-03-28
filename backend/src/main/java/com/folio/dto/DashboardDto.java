package com.folio.dto;
import java.util.List;
public final class DashboardDto {
    private Double totalPortfolioValue;
    private Integer stockCount;
    private Double totalDividendRatio;
    private List<HoldingDto> top5Holdings;
    private List<DividendSourceDto> top5DividendSources;
    private String lastQuoteFetchAt;
    public DashboardDto() {}
    public Double getTotalPortfolioValue() { return totalPortfolioValue; }
    public void setTotalPortfolioValue(Double totalPortfolioValue) { this.totalPortfolioValue = totalPortfolioValue; }
    public Integer getStockCount() { return stockCount; }
    public void setStockCount(Integer stockCount) { this.stockCount = stockCount; }
    public Double getTotalDividendRatio() { return totalDividendRatio; }
    public void setTotalDividendRatio(Double totalDividendRatio) { this.totalDividendRatio = totalDividendRatio; }
    public List<HoldingDto> getTop5Holdings() { return top5Holdings; }
    public void setTop5Holdings(List<HoldingDto> top5Holdings) { this.top5Holdings = top5Holdings; }
    public List<DividendSourceDto> getTop5DividendSources() { return top5DividendSources; }
    public void setTop5DividendSources(List<DividendSourceDto> top5DividendSources) { this.top5DividendSources = top5DividendSources; }
    public String getLastQuoteFetchAt() { return lastQuoteFetchAt; }
    public void setLastQuoteFetchAt(String lastQuoteFetchAt) { this.lastQuoteFetchAt = lastQuoteFetchAt; }
    public static DashboardDtoBuilder builder() { return new DashboardDtoBuilder(); }
}