package com.folio.quote.yahoo;

import com.folio.domain.Quote;
import com.folio.domain.TickerSymbol;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

final class QuoteFetcherIntegrationTest {

    private final QuoteFetcher quoteFetcher = new QuoteFetcher();

    @Test
    void fetchesQuoteForApple() {
        // given
        TickerSymbol aapl = new TickerSymbol("AAPL");

        // when
        Optional<Quote> result = quoteFetcher.fetchQuote(aapl);
        result.ifPresent(q -> System.out.printf(
                "AAPL  price=%.2f  currency=%s  time=%s%n",
                q.amount().value(),
                q.amount().currency(),
                q.timestamp()));

        // then
        assertThat(result).isPresent();
        assertThat(result.get().amount().value()).isGreaterThan(0);
        assertThat(result.get().timestamp()).isNotNull();
    }

    @Test
    void fetchesQuoteForMicrosoft() {
        // given
        TickerSymbol msft = new TickerSymbol("MSFT");

        // when
        Optional<Quote> result = quoteFetcher.fetchQuote(msft);
        result.ifPresent(q -> System.out.printf(
                "MSFT  price=%.2f  currency=%s  time=%s%n",
                q.amount().value(),
                q.amount().currency(),
                q.timestamp()));

        // then
        assertThat(result).isPresent();
        assertThat(result.get().amount().value()).isGreaterThan(0);
        assertThat(result.get().timestamp()).isNotNull();
    }

    @Test
    void returnsEmptyForUnknownTicker() {
        // given
        TickerSymbol unknown = new TickerSymbol("ZZZZZZZZZZ");

        // when
        Optional<Quote> result = quoteFetcher.fetchQuote(unknown);
        System.out.println("ZZZZZZZZZZ → " + result.map(q -> q.amount().value()).orElse(null));

        // then
        assertThat(result).isEmpty();
    }
}
