package com.folio.dto;

/**
 * Groups aggregate statistics for the transaction list.
 */
record TransactionAggregates(long filteredCount, Double sumCount) {
}

