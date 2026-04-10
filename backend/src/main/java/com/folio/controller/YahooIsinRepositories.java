package com.folio.controller;

import com.folio.repository.IsinRepository;
import com.folio.repository.TickerSymbolRepository;

/**
 * Groups repository dependencies for {@link YahooIsinController}.
 */
record YahooIsinRepositories(IsinRepository isinRepository, TickerSymbolRepository tickerSymbolRepository) {}

