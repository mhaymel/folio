package com.folio.dto;

import com.folio.domain.Isin;

public final class StockDtoBuilder {
    private Isin isin;
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

    public StockDtoBuilder isin(Isin isin) { this.isin = isin; return this; }
    public StockDtoBuilder name(String name) { this.name = name; return this; }
    public StockDtoBuilder country(String country) { this.country = country; return this; }
    public StockDtoBuilder branch(String branch) { this.branch = branch; return this; }
    public StockDtoBuilder depot(String depot) { this.depot = depot; return this; }
    public StockDtoBuilder count(Double count) { this.count = count; return this; }
    public StockDtoBuilder avgEntryPrice(Double avgEntryPrice) { this.avgEntryPrice = avgEntryPrice; return this; }
    public StockDtoBuilder currentQuote(Double currentQuote) { this.currentQuote = currentQuote; return this; }
    public StockDtoBuilder performancePercent(Double performancePercent) { this.performancePercent = performancePercent; return this; }
    public StockDtoBuilder dividendPerShare(Double dividendPerShare) { this.dividendPerShare = dividendPerShare; return this; }
    public StockDtoBuilder estimatedAnnualIncome(Double estimatedAnnualIncome) { this.estimatedAnnualIncome = estimatedAnnualIncome; return this; }

    public StockDto build() {
        StockDto s = new StockDto();
        s.setIsin(isin); s.setName(name); s.setCountry(country); s.setBranch(branch);
        s.setDepot(depot); s.setCount(count); s.setAvgEntryPrice(avgEntryPrice);
        s.setCurrentQuote(currentQuote); s.setPerformancePercent(performancePercent);
        s.setDividendPerShare(dividendPerShare); s.setEstimatedAnnualIncome(estimatedAnnualIncome);
        return s;
    }
}
