package com.folio.dto;

public final class StockDto {
    private String isin;
    private String name;
    private String country;
    private String branch;
    private String depot;
    private Double count;
    private Double avgEntryPrice;
    private Double currentQuote;
    private Double performancePercent;
    private Double dividendPerShare;
    private Double estimatedAnnualIncome;

    public StockDto() {}

    public String getIsin() { return isin; }
    public void setIsin(String isin) { this.isin = isin; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
    public String getDepot() { return depot; }
    public void setDepot(String depot) { this.depot = depot; }
    public Double getCount() { return count; }
    public void setCount(Double count) { this.count = count; }
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

    public static StockDtoBuilder builder() { return new StockDtoBuilder(); }
}
