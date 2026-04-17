package com.folio.dto;

import java.time.LocalDateTime;

/**
 * Components are nullable because Jackson deserialization builds the outer
 * {@link YahooQuoteWithQuoteDto} incrementally via setters.
 */
public record QuoteTiming(String fetchedAt, LocalDateTime rawFetchedAt) {
}
