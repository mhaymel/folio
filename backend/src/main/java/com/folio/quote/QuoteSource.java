package com.folio.quote;

import com.folio.domain.IsinCode;

import java.util.Optional;

/**
 * A single quote source in the cascade fallback chain.
 * Each implementation fetches an EUR-denominated price for one ISIN.
 */
public interface QuoteSource {

    /**
     * @return human-readable name matching a quote_provider entry (e.g. "JustETF", "Onvista")
     */
    String providerName();

    /**
     * Try to fetch an EUR price for the given ISIN.
     *
     * @param isin the validated ISIN code
     * @return the price in EUR, or empty if this source cannot resolve it
     */
    Optional<Double> fetchQuote(IsinCode isin);
}

