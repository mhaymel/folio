package com.folio.dto;

import com.folio.domain.Isin;
import com.folio.domain.TickerSymbol;

/**
 * Groups core security identification fields shared across multiple DTOs.
 * Components are nullable because Jackson deserialization builds DTOs incrementally.
 */
public record SecurityIdentity(Isin isin, TickerSymbol tickerSymbol, String name) {
}
