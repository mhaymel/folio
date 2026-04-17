package com.folio.dto;

/**
 * Components are nullable because Jackson deserialization builds the outer
 * {@link TransactionDto} incrementally via setters.
 */
public record TransactionTradeData(String depot, Double count, Double sharePrice) {
}
