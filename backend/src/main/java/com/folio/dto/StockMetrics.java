package com.folio.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

/**
 * Groups all numeric metrics (position and performance) for a stock.
 */
public record StockMetrics(@JsonUnwrapped StockPosition position, @JsonUnwrapped StockPerformance performance) {
}
