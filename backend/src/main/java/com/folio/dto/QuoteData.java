package com.folio.dto;

/**
 * Groups quote value data for a Yahoo quote DTO.
 */
public record QuoteData(Double price, String currency, String provider) {
}

