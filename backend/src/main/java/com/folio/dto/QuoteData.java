package com.folio.dto;

/**
 * Components are nullable because Jackson deserialization builds the outer
 * {@link YahooQuoteWithQuoteDto} incrementally via setters.
 */
public record QuoteData(Double price, String currency, String provider) {
}
