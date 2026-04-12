package com.folio.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import static java.util.Objects.requireNonNull;

/**
 * Groups all numeric metrics (position and performance) for a stock.
 */
public record StockMetrics(@JsonUnwrapped StockPosition position, @JsonUnwrapped StockPerformance performance) {
    public StockMetrics {
        requireNonNull(position);
        requireNonNull(performance);
    }
}
