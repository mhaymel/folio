package com.folio.dto;

import java.time.LocalDateTime;

/**
 * Filter criteria for querying transactions.
 */
public record TransactionFilter(
    String isin,
    String depot,
    LocalDateTime fromDate,
    LocalDateTime toDate
) {
    public static TransactionFilter none() {
        return new TransactionFilter(null, null, null, null);
    }
}

