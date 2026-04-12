package com.folio.dto;

/**
 * Groups position sizing data for a stock.
 */
public record StockPosition(Double count, Double avgEntryPrice, Double currentQuote) {
}

