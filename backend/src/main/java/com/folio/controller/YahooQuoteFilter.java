package com.folio.controller;

import static java.util.Objects.requireNonNull;

record YahooQuoteFilter(String isinFragment, String nameFragment, String tickerFragment) {

    YahooQuoteFilter {
        requireNonNull(isinFragment);
        requireNonNull(nameFragment);
        requireNonNull(tickerFragment);
    }
}

