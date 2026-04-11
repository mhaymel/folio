package com.folio.dto;

import static java.util.Objects.requireNonNull;

public record YahooIsinWithoutTickerItem(String isin, String name) {
    public YahooIsinWithoutTickerItem {
        requireNonNull(isin);
        requireNonNull(name);
    }
}
