package com.folio.dto;

import com.folio.domain.Isin;
import com.folio.domain.TickerSymbol;

/**
 * Groups core security identification fields shared across multiple DTOs.
 */
public record SecurityIdentity(Isin isin, TickerSymbol tickerSymbol, String name) {
}
