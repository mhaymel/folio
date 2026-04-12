package com.folio.dto;

import com.folio.domain.Isin;

/**
 * Groups core security identification fields shared across multiple DTOs.
 */
public record SecurityIdentity(Isin isin, String tickerSymbol, String name) {
}

