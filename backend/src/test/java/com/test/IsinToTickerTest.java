package com.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.folio.service.IsinToTicker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class IsinToTickerTest {

    private HttpClient httpClient;
    private IsinToTicker isinToTicker;

    @SuppressWarnings("unchecked")
    private void stubResponse(int statusCode, String body) throws Exception {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(statusCode);
        when(response.body()).thenReturn(body);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(response);
    }

    @BeforeEach
    void setUp() {
        httpClient = mock(HttpClient.class);
        isinToTicker = new IsinToTicker(httpClient, new ObjectMapper());
    }

    // --- tickerFor (single) ---

    @Test
    void returnsTicker() throws Exception {
        String json = """
                [{"data":[{"figi":"BBG000B9XRY4","ticker":"AAPL","name":"APPLE INC","exchCode":"US"}]}]
                """;
        stubResponse(200, json);

        Optional<String> ticker = isinToTicker.tickerFor("US0378331005");

        assertThat(ticker).isPresent().contains("AAPL");
    }

    @Test
    void returnsEmptyForErrorResponse() throws Exception {
        String json = """
                [{"error":"No identifier found."}]
                """;
        stubResponse(200, json);

        Optional<String> ticker = isinToTicker.tickerFor("INVALID_ISIN");

        assertThat(ticker).isEmpty();
    }

    @Test
    void returnsEmptyForEmptyDataArray() throws Exception {
        String json = """
                [{"data":[]}]
                """;
        stubResponse(200, json);

        Optional<String> ticker = isinToTicker.tickerFor("US0000000000");

        assertThat(ticker).isEmpty();
    }

    @Test
    void returnsEmptyForNon200Status() throws Exception {
        stubResponse(429, "Too Many Requests");

        Optional<String> ticker = isinToTicker.tickerFor("US0378331005");

        assertThat(ticker).isEmpty();
    }

    @SuppressWarnings("unchecked")
    @Test
    void returnsEmptyOnException() throws Exception {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new RuntimeException("connection refused"));

        Optional<String> ticker = isinToTicker.tickerFor("US0378331005");

        assertThat(ticker).isEmpty();
    }

    @Test
    void throwsOnNullIsin() {
        assertThatThrownBy(() -> isinToTicker.tickerFor(null))
                .isInstanceOf(NullPointerException.class);
    }

    // --- tickersFor (batch) ---

    @Test
    void batchReturnsTickersForMultipleIsins() throws Exception {
        String json = """
                [
                  {"data":[{"ticker":"AAPL"}]},
                  {"data":[{"ticker":"MSFT"}]}
                ]
                """;
        stubResponse(200, json);

        Map<String, Optional<String>> result =
                isinToTicker.tickersFor(List.of("US0378331005", "US5949181045"));

        assertThat(result).hasSize(2);
        assertThat(result.get("US0378331005")).contains("AAPL");
        assertThat(result.get("US5949181045")).contains("MSFT");
    }

    @Test
    void batchHandlesPartialFailures() throws Exception {
        String json = """
                [
                  {"data":[{"ticker":"AAPL"}]},
                  {"error":"No identifier found."}
                ]
                """;
        stubResponse(200, json);

        Map<String, Optional<String>> result =
                isinToTicker.tickersFor(List.of("US0378331005", "XX0000000000"));

        assertThat(result.get("US0378331005")).contains("AAPL");
        assertThat(result.get("XX0000000000")).isEmpty();
    }

    @Test
    void batchReturnsEmptyMapForEmptyList() {
        Map<String, Optional<String>> result = isinToTicker.tickersFor(List.of());

        assertThat(result).isEmpty();
    }

    @Test
    void batchReturnsEmptyForNon200() throws Exception {
        stubResponse(500, "Internal Server Error");

        Map<String, Optional<String>> result =
                isinToTicker.tickersFor(List.of("US0378331005"));

        assertThat(result.get("US0378331005")).isEmpty();
    }

    @SuppressWarnings("unchecked")
    @Test
    void batchReturnsEmptyOnException() throws Exception {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new RuntimeException("timeout"));

        Map<String, Optional<String>> result =
                isinToTicker.tickersFor(List.of("US0378331005"));

        assertThat(result.get("US0378331005")).isEmpty();
    }

    @Test
    void batchThrowsOnNullList() {
        assertThatThrownBy(() -> isinToTicker.tickersFor(null))
                .isInstanceOf(NullPointerException.class);
    }
}

