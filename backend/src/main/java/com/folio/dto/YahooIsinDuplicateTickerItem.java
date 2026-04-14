package com.folio.dto;

import com.folio.domain.TickerSymbol;

import static java.util.Objects.requireNonNull;

public record YahooIsinDuplicateTickerItem(String isin, TickerSymbol tickerSymbol, String name) {
    public YahooIsinDuplicateTickerItem {
        requireNonNull(isin);
        requireNonNull(tickerSymbol);
        requireNonNull(name);
    }
}