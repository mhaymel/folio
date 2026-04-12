package com.folio.dto;

/**
 * Groups performance and dividend metrics for a stock.
 */
public record StockPerformance(Double performancePercent, Double dividendPerShare, Double estimatedAnnualIncome) {
}

