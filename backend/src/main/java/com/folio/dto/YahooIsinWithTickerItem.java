package com.folio.dto;

import static java.util.Objects.requireNonNull;

public record YahooIsinWithTickerItem(String isin, String tickerSymbol, String name) {
    public YahooIsinWithTickerItem {
        requireNonNull(isin);
        requireNonNull(tickerSymbol);
        requireNonNull(name);
    }
}
