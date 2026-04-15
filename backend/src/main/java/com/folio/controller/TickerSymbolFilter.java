package com.folio.controller;

import static java.util.Objects.requireNonNull;

record TickerSymbolFilter(String isinFragment, String tickerSymbolFragment, String nameFragment) {

    TickerSymbolFilter {
        requireNonNull(isinFragment);
        requireNonNull(tickerSymbolFragment);
        requireNonNull(nameFragment);
    }
}

