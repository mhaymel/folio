package com.folio.dto;

import java.time.LocalDateTime;

/**
 * Filter criteria for querying transactions. Every component is nullable — a
 * null value means "no constraint on that field" (see {@link #none()}).
 */
public record TransactionFilter(
    String isinFragment,
    String tickerSymbolFragment,
    String nameFragment,
    String depotFragment,
    LocalDateTime fromDate,
    LocalDateTime toDate
) {
    public static TransactionFilter none() {
        return new TransactionFilter(null, null, null, null, null, null);
    }
}

