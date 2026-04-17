package com.folio.dto;

import com.folio.domain.Isin;
import com.folio.domain.TickerSymbol;

public record SecurityIdentity(Isin isin, TickerSymbol tickerSymbol, String name) {
}
