package com.test;

import com.folio.domain.Quote;
import com.folio.domain.TickerSymbol;
import com.folio.online.yahoo.QuoteFetcher;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

final class YahooQuoteFetcherTest {

    private final QuoteFetcher quoteFetcher = new QuoteFetcher();

    @Test
    void fetchesQuoteForAxaParis() {
        // CS.PA = AXA SA on Euronext Paris (ISIN FR0000120628)
        Optional<Quote> quote = quoteFetcher.fetchQuote(new TickerSymbol("CS.PA"));
        System.out.println("CS.PA → " + quote.map(q ->
            q.amount().value() + " " + q.amount().currency().name()).orElse("NOT FOUND"));

        assertThat(quote).isPresent();
        assertThat(quote.get().amount().value()).isGreaterThan(0);
    }
}
