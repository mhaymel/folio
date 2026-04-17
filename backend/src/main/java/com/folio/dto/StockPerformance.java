package com.folio.dto;

/**
 * All components are nullable: performance cannot be computed without a current
 * quote, and dividend fields are absent when no dividend is recorded for the ISIN.
 */
public record StockPerformance(Double performancePercent, Double dividendPerShare, Double estimatedAnnualIncome) {
}
