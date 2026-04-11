package com.folio.dto;

import static java.util.Objects.requireNonNull;

public record YahooIsinDuplicateTickerItem(String isin, String tickerSymbol, String name) {
    public YahooIsinDuplicateTickerItem {
        requireNonNull(isin);
        requireNonNull(tickerSymbol);
        requireNonNull(name);
    }
}
