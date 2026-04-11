package com.folio.online.yahoo;

import com.folio.domain.Isin;
import com.folio.domain.TickerSymbol;

import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Result of a Yahoo Finance ISIN → ticker lookup.
 *
 * @param found    map of ISIN → ticker symbol for every ISIN that was resolved
 * @param notFound set of ISINs for which no ticker symbol could be found
 */
public record IsinTickerSearchResult(Map<Isin, TickerSymbol> found, Set<Isin> notFound) {
    public IsinTickerSearchResult {
        requireNonNull(found);
        requireNonNull(notFound);
        found = Map.copyOf(found);
        notFound = Set.copyOf(notFound);
    }
}
