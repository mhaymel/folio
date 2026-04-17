package com.folio.dto;

/**
 * {@code country} and {@code branch} are nullable when no mapping exists for the ISIN.
 */
public record StockClassification(String country, String branch, String depot) {
}
