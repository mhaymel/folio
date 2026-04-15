package com.folio.controller;

import static java.util.Objects.requireNonNull;

record StockFilter(String isinFragment, String tickerSymbolFragment, String nameFragment,
                   String depotFragment, String countryFragment, String branchFragment) {

    StockFilter {
        requireNonNull(isinFragment);
        requireNonNull(tickerSymbolFragment);
        requireNonNull(nameFragment);
        requireNonNull(depotFragment);
        requireNonNull(countryFragment);
        requireNonNull(branchFragment);
    }
}

