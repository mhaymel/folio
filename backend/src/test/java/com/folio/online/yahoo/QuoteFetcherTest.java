package com.folio.online.yahoo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.folio.domain.Currency;
import com.folio.domain.Quote;
import com.folio.domain.TickerSymbol;
import com.folio.online.yahoo.QuoteFetcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class QuoteFetcherTest {

    private static final TickerSymbol AAPL = new TickerSymbol("AAPL");

    private HttpClient httpClient;
    private QuoteFetcher quoteFetcher;

    @BeforeEach
    void setUp() {
        httpClient = mock(HttpClient.class);
        quoteFetcher = new QuoteFetcher(httpClient, new ObjectMapper());
    }

    @SuppressWarnings("unchecked")
    private void stubResponse(int statusCode, String body) throws Exception {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(statusCode);
        when(response.body()).thenReturn(body);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(response);
    }

    @Test
    void returnsQuoteWithPriceCurrencyAndTimestamp() throws Exception {
        // given
        stubResponse(200, """
                {
                  "chart": {
                    "result": [{
                      "meta": {
                        "regularMarketPrice": 182.63,
                        "currency": "USD",
                        "regularMarketTime": 1743693600
                      }
                    }]
                  }
                }
                """);

        // when
        Optional<Quote> result = quoteFetcher.fetchQuote(AAPL);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().amount().value()).isEqualTo(182.63);
        assertThat(result.get().amount().currency()).isEqualTo(Currency.USD);
        assertThat(result.get().timestamp()).isEqualTo(Instant.ofEpochSecond(1743693600));
    }

    @Test
    void returnsEmptyForNon200Status() throws Exception {
        // given
        stubResponse(404, "Not Found");

        // when
        Optional<Quote> result = quoteFetcher.fetchQuote(AAPL);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void returnsEmptyWhenResultArrayIsMissing() throws Exception {
        // given
        stubResponse(200, """
                {"chart":{"result":null,"error":{"code":"Not Found"}}}
                """);

        // when
        Optional<Quote> result = quoteFetcher.fetchQuote(AAPL);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void returnsEmptyWhenPriceIsZero() throws Exception {
        // given
        stubResponse(200, """
                {"chart":{"result":[{"meta":{"regularMarketPrice":0.0,"currency":"USD","regularMarketTime":1743693600}}]}}
                """);

        // when
        Optional<Quote> result = quoteFetcher.fetchQuote(AAPL);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void returnsEmptyWhenPriceIsNegative() throws Exception {
        // given
        stubResponse(200, """
                {"chart":{"result":[{"meta":{"regularMarketPrice":-5.0,"currency":"USD","regularMarketTime":1743693600}}]}}
                """);

        // when
        Optional<Quote> result = quoteFetcher.fetchQuote(AAPL);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void returnsEmptyWhenCurrencyIsMissing() throws Exception {
        // given
        stubResponse(200, """
                {"chart":{"result":[{"meta":{"regularMarketPrice":182.63,"regularMarketTime":1743693600}}]}}
                """);

        // when
        Optional<Quote> result = quoteFetcher.fetchQuote(AAPL);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void returnsEmptyWhenTimestampIsMissing() throws Exception {
        // given
        stubResponse(200, """
                {"chart":{"result":[{"meta":{"regularMarketPrice":182.63,"currency":"USD"}}]}}
                """);

        // when
        Optional<Quote> result = quoteFetcher.fetchQuote(AAPL);

        // then
        assertThat(result).isEmpty();
    }

    @SuppressWarnings("unchecked")
    @Test
    void returnsEmptyOnNetworkException() throws Exception {
        // given
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new RuntimeException("connection refused"));

        // when
        Optional<Quote> result = quoteFetcher.fetchQuote(AAPL);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void throwsOnNullTickerSymbol() {
        assertThatThrownBy(() -> quoteFetcher.fetchQuote(null))
                .isInstanceOf(NullPointerException.class);
    }
}

