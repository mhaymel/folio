package com.folio.dto;

/**
 * Groups classification and depot assignment for a stock.
 */
public record StockClassification(String country, String branch, String depot) {
}

