package com.folio.online.yahoo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.folio.domain.Isin;
import com.folio.domain.TickerSymbol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class IsinTickerSearchTest {

    private static final Isin AXA   = new Isin("FR0000120628");
    private static final Isin HP    = new Isin("US40434L1052");
    private static final Isin BOGUS = new Isin("XX0000000000");

    private HttpClient httpClient;
    private IsinTickerSearch search;

    @BeforeEach
    void setUp() {
        httpClient = mock(HttpClient.class);
        search = new IsinTickerSearch(httpClient, new ObjectMapper());
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
    void returnsTickerForSingleIsin() throws Exception {
        stubResponse(200, """
                {"quotes":[{"symbol":"CS.PA","quoteType":"EQUITY","isYahooFinance":true}]}
                """);

        IsinTickerSearchResult result = search.search(Set.of(AXA));

        assertThat(result.found()).containsEntry(AXA, new TickerSymbol("CS.PA"));
        assertThat(result.notFound()).isEmpty();
    }

    @Test
    void putsIsinInNotFoundWhenQuotesArrayIsEmpty() throws Exception {
        stubResponse(200, """
                {"quotes":[]}
                """);

        IsinTickerSearchResult result = search.search(Set.of(BOGUS));

        assertThat(result.found()).isEmpty();
        assertThat(result.notFound()).containsExactly(BOGUS);
    }

    @Test
    void skipsQuotesWithIsYahooFinanceFalse() throws Exception {
        stubResponse(200, """
                {"quotes":[
                  {"symbol":"SKIP","isYahooFinance":false},
                  {"symbol":"HPQ","isYahooFinance":true}
                ]}
                """);

        IsinTickerSearchResult result = search.search(Set.of(HP));

        assertThat(result.found()).containsEntry(HP, new TickerSymbol("HPQ"));
    }

    @Test
    void putsIsinInNotFoundOnNon200Response() throws Exception {
        stubResponse(429, "Too Many Requests");

        IsinTickerSearchResult result = search.search(Set.of(AXA));

        assertThat(result.found()).isEmpty();
        assertThat(result.notFound()).containsExactly(AXA);
    }

    @SuppressWarnings("unchecked")
    @Test
    void putsIsinInNotFoundOnNetworkException() throws Exception {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new RuntimeException("connection refused"));

        IsinTickerSearchResult result = search.search(Set.of(AXA));

        assertThat(result.found()).isEmpty();
        assertThat(result.notFound()).containsExactly(AXA);
    }

    @Test
    void handlesEmptyInputSet() {
        IsinTickerSearchResult result = search.search(Set.of());

        assertThat(result.found()).isEmpty();
        assertThat(result.notFound()).isEmpty();
    }

    @Test
    void throwsOnNullInput() {
        assertThatThrownBy(() -> search.search(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void putsIsinInNotFoundWhenQuotesNodeIsMissing() throws Exception {
        stubResponse(200, """
                {"news":[]}
                """);

        IsinTickerSearchResult result = search.search(Set.of(BOGUS));

        assertThat(result.found()).isEmpty();
        assertThat(result.notFound()).containsExactly(BOGUS);
    }
}
