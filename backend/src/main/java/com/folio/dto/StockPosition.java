package com.folio.dto;

/**
 * {@code currentQuote} is nullable when no quote is available for the ISIN.
 */
public record StockPosition(Double count, Double avgEntryPrice, Double currentQuote) {
}
