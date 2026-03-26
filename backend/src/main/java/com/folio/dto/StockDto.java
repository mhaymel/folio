package com.folio.dto;

public class StockDto {
    private String isin;
    private String name;
    private String country;
    private String branch;
    private Double totalShares;
    private Double avgEntryPrice;
    private Double currentQuote;
    private Double performancePercent;
    private Double dividendPerShare;
    private Double estimatedAnnualIncome;

    public StockDto() {}

    public StockDto(String isin, String name, String country, String branch, Double totalShares,
                       Double avgEntryPrice, Double currentQuote, Double performancePercent,
                       Double dividendPerShare, Double estimatedAnnualIncome) {
        this.isin = isin;
        this.name = name;
        this.country = country;
        this.branch = branch;
        this.totalShares = totalShares;
        this.avgEntryPrice = avgEntryPrice;
        this.currentQuote = currentQuote;
        this.performancePercent = performancePercent;
        this.dividendPerShare = dividendPerShare;
        this.estimatedAnnualIncome = estimatedAnnualIncome;
    }

    public String getIsin() { return isin; }
    public void setIsin(String isin) { this.isin = isin; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
    public Double getTotalShares() { return totalShares; }
    public void setTotalShares(Double totalShares) { this.totalShares = totalShares; }
    public Double getAvgEntryPrice() { return avgEntryPrice; }
    public void setAvgEntryPrice(Double avgEntryPrice) { this.avgEntryPrice = avgEntryPrice; }
    public Double getCurrentQuote() { return currentQuote; }
    public void setCurrentQuote(Double currentQuote) { this.currentQuote = currentQuote; }
    public Double getPerformancePercent() { return performancePercent; }
    public void setPerformancePercent(Double performancePercent) { this.performancePercent = performancePercent; }
    public Double getDividendPerShare() { return dividendPerShare; }
    public void setDividendPerShare(Double dividendPerShare) { this.dividendPerShare = dividendPerShare; }
    public Double getEstimatedAnnualIncome() { return estimatedAnnualIncome; }
    public void setEstimatedAnnualIncome(Double estimatedAnnualIncome) { this.estimatedAnnualIncome = estimatedAnnualIncome; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String isin;
        private String name;
        private String country;
        private String branch;
        private Double totalShares;
        private Double avgEntryPrice;
        private Double currentQuote;
        private Double performancePercent;
        private Double dividendPerShare;
        private Double estimatedAnnualIncome;
        public Builder isin(String isin) { this.isin = isin; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder country(String country) { this.country = country; return this; }
        public Builder branch(String branch) { this.branch = branch; return this; }
        public Builder totalShares(Double totalShares) { this.totalShares = totalShares; return this; }
        public Builder avgEntryPrice(Double avgEntryPrice) { this.avgEntryPrice = avgEntryPrice; return this; }
        public Builder currentQuote(Double currentQuote) { this.currentQuote = currentQuote; return this; }
        public Builder performancePercent(Double performancePercent) { this.performancePercent = performancePercent; return this; }
        public Builder dividendPerShare(Double dividendPerShare) { this.dividendPerShare = dividendPerShare; return this; }
        public Builder estimatedAnnualIncome(Double estimatedAnnualIncome) { this.estimatedAnnualIncome = estimatedAnnualIncome; return this; }
        public StockDto build() {
            return new StockDto(isin, name, country, branch, totalShares, avgEntryPrice,
                    currentQuote, performancePercent, dividendPerShare, estimatedAnnualIncome);
        }
    }
}

