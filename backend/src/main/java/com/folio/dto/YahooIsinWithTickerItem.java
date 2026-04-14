package com.folio.dto;

import com.folio.domain.TickerSymbol;

import static java.util.Objects.requireNonNull;

public record YahooIsinWithTickerItem(String isin, TickerSymbol tickerSymbol, String name) {
    public YahooIsinWithTickerItem {
        requireNonNull(isin);
        requireNonNull(tickerSymbol);
        requireNonNull(name);
    }
}