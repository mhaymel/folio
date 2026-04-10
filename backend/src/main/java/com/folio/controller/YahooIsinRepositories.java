package com.folio.controller;

import com.folio.repository.IsinRepository;
import com.folio.repository.TickerSymbolRepository;

import static java.util.Objects.requireNonNull;

/**
 * Groups repository dependencies for {@link YahooIsinController}.
 */
record YahooIsinRepositories(IsinRepository isinRepository, TickerSymbolRepository tickerSymbolRepository) {
    YahooIsinRepositories {
        requireNonNull(isinRepository);
        requireNonNull(tickerSymbolRepository);
    }
}

