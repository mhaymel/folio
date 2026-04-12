package com.folio.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.folio.domain.Isin;

public final class StockDto {
    private SecurityIdentity security;
    private StockClassification classification;
    private StockMetrics metrics;

    public StockDto() {
        this.security = new SecurityIdentity(null, null, null);
        this.classification = new StockClassification(null, null, null);
        this.metrics = new StockMetrics(
                new StockPosition(null, null, null),
                new StockPerformance(null, null, null));
    }

    public StockDto(SecurityIdentity security, StockClassification classification, StockMetrics metrics) {
        this.security = security;
        this.classification = classification;
        this.metrics = metrics;
    }

    @JsonUnwrapped
    public SecurityIdentity getSecurity() { return security; }
    @JsonUnwrapped
    public StockClassification getClassification() { return classification; }
    @JsonUnwrapped
    public StockMetrics getMetrics() { return metrics; }

    public Isin getIsin() { return security.isin(); }
    public void setIsin(Isin isin) { this.security = new SecurityIdentity(isin, security.tickerSymbol(), security.name()); }
    public String getTickerSymbol() { return security.tickerSymbol(); }
    public void setTickerSymbol(String tickerSymbol) { this.security = new SecurityIdentity(security.isin(), tickerSymbol, security.name()); }
    public String getName() { return security.name(); }
    public void setName(String name) { this.security = new SecurityIdentity(security.isin(), security.tickerSymbol(), name); }
    public String getCountry() { return classification.country(); }
    public void setCountry(String country) { this.classification = new StockClassification(country, classification.branch(), classification.depot()); }
    public String getBranch() { return classification.branch(); }
    public void setBranch(String branch) { this.classification = new StockClassification(classification.country(), branch, classification.depot()); }
    public String getDepot() { return classification.depot(); }
    public void setDepot(String depot) { this.classification = new StockClassification(classification.country(), classification.branch(), depot); }
    public Double getCount() { return metrics.position().count(); }
    public void setCount(Double count) { this.metrics = new StockMetrics(new StockPosition(count, metrics.position().avgEntryPrice(), metrics.position().currentQuote()), metrics.performance()); }
    public Double getAvgEntryPrice() { return metrics.position().avgEntryPrice(); }
    public void setAvgEntryPrice(Double avgEntryPrice) { this.metrics = new StockMetrics(new StockPosition(metrics.position().count(), avgEntryPrice, metrics.position().currentQuote()), metrics.performance()); }
    public Double getCurrentQuote() { return metrics.position().currentQuote(); }
    public void setCurrentQuote(Double currentQuote) { this.metrics = new StockMetrics(new StockPosition(metrics.position().count(), metrics.position().avgEntryPrice(), currentQuote), metrics.performance()); }
    public Double getPerformancePercent() { return metrics.performance().performancePercent(); }
    public void setPerformancePercent(Double performancePercent) { this.metrics = new StockMetrics(metrics.position(), new StockPerformance(performancePercent, metrics.performance().dividendPerShare(), metrics.performance().estimatedAnnualIncome())); }
    public Double getDividendPerShare() { return metrics.performance().dividendPerShare(); }
    public void setDividendPerShare(Double dividendPerShare) { this.metrics = new StockMetrics(metrics.position(), new StockPerformance(metrics.performance().performancePercent(), dividendPerShare, metrics.performance().estimatedAnnualIncome())); }
    public Double getEstimatedAnnualIncome() { return metrics.performance().estimatedAnnualIncome(); }
    public void setEstimatedAnnualIncome(Double estimatedAnnualIncome) { this.metrics = new StockMetrics(metrics.position(), new StockPerformance(metrics.performance().performancePercent(), metrics.performance().dividendPerShare(), estimatedAnnualIncome)); }

}
