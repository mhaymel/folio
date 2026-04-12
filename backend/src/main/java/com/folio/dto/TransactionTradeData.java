package com.folio.dto;

/**
 * Groups trade execution data for a transaction.
 */
public record TransactionTradeData(String depot, Double count, Double sharePrice) {
}

