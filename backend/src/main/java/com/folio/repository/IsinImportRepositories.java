package com.folio.repository;

import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/**
 * Groups ISIN-related repositories used by import operations.
 */
@Component
record IsinImportRepositories(IsinRepository isin, IsinNameRepository isinName, TickerSymbolRepository tickerSymbol) {
    IsinImportRepositories {
        requireNonNull(isin);
        requireNonNull(isinName);
        requireNonNull(tickerSymbol);
    }
}

