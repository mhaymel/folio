package com.folio.dto;

/**
 * Groups portfolio summary metrics for the dashboard.
 */
public record DashboardSummary(Double totalPortfolioValue, Integer stockCount, Double totalDividendRatio) {
}

