package com.folio.dto;

import java.time.LocalDateTime;
import java.util.List;

public class DashboardDto {
    private Double totalPortfolioValue;
    private Integer stockCount;
    private Double totalDividendRatio;
    private List<HoldingDto> top5Holdings;
    private List<DividendSourceDto> top5DividendSources;
    private LocalDateTime lastQuoteFetchAt;

    public DashboardDto() {}

    public DashboardDto(Double totalPortfolioValue, Integer stockCount, Double totalDividendRatio,
                        List<HoldingDto> top5Holdings, List<DividendSourceDto> top5DividendSources,
                        LocalDateTime lastQuoteFetchAt) {
        this.totalPortfolioValue = totalPortfolioValue;
        this.stockCount = stockCount;
        this.totalDividendRatio = totalDividendRatio;
        this.top5Holdings = top5Holdings;
        this.top5DividendSources = top5DividendSources;
        this.lastQuoteFetchAt = lastQuoteFetchAt;
    }

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
    public LocalDateTime getLastQuoteFetchAt() { return lastQuoteFetchAt; }
    public void setLastQuoteFetchAt(LocalDateTime lastQuoteFetchAt) { this.lastQuoteFetchAt = lastQuoteFetchAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Double totalPortfolioValue;
        private Integer stockCount;
        private Double totalDividendRatio;
        private List<HoldingDto> top5Holdings;
        private List<DividendSourceDto> top5DividendSources;
        private LocalDateTime lastQuoteFetchAt;
        public Builder totalPortfolioValue(Double v) { this.totalPortfolioValue = v; return this; }
        public Builder stockCount(Integer v) { this.stockCount = v; return this; }
        public Builder totalDividendRatio(Double v) { this.totalDividendRatio = v; return this; }
        public Builder top5Holdings(List<HoldingDto> v) { this.top5Holdings = v; return this; }
        public Builder top5DividendSources(List<DividendSourceDto> v) { this.top5DividendSources = v; return this; }
        public Builder lastQuoteFetchAt(LocalDateTime v) { this.lastQuoteFetchAt = v; return this; }
        public DashboardDto build() {
            return new DashboardDto(totalPortfolioValue, stockCount, totalDividendRatio,
                    top5Holdings, top5DividendSources, lastQuoteFetchAt);
        }
    }

    public static class HoldingDto {
        private String isin;
        private String name;
        private Double investedAmount;

        public HoldingDto() {}

        public HoldingDto(String isin, String name, Double investedAmount) {
            this.isin = isin;
            this.name = name;
            this.investedAmount = investedAmount;
        }

        public String getIsin() { return isin; }
        public void setIsin(String isin) { this.isin = isin; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Double getInvestedAmount() { return investedAmount; }
        public void setInvestedAmount(Double investedAmount) { this.investedAmount = investedAmount; }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private String isin;
            private String name;
            private Double investedAmount;
            public Builder isin(String isin) { this.isin = isin; return this; }
            public Builder name(String name) { this.name = name; return this; }
            public Builder investedAmount(Double investedAmount) { this.investedAmount = investedAmount; return this; }
            public HoldingDto build() { return new HoldingDto(isin, name, investedAmount); }
        }
    }

    public static class DividendSourceDto {
        private String isin;
        private String name;
        private Double estimatedAnnualIncome;

        public DividendSourceDto() {}

        public DividendSourceDto(String isin, String name, Double estimatedAnnualIncome) {
            this.isin = isin;
            this.name = name;
            this.estimatedAnnualIncome = estimatedAnnualIncome;
        }

        public String getIsin() { return isin; }
        public void setIsin(String isin) { this.isin = isin; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Double getEstimatedAnnualIncome() { return estimatedAnnualIncome; }
        public void setEstimatedAnnualIncome(Double estimatedAnnualIncome) { this.estimatedAnnualIncome = estimatedAnnualIncome; }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private String isin;
            private String name;
            private Double estimatedAnnualIncome;
            public Builder isin(String isin) { this.isin = isin; return this; }
            public Builder name(String name) { this.name = name; return this; }
            public Builder estimatedAnnualIncome(Double estimatedAnnualIncome) { this.estimatedAnnualIncome = estimatedAnnualIncome; return this; }
            public DividendSourceDto build() { return new DividendSourceDto(isin, name, estimatedAnnualIncome); }
        }
    }
}