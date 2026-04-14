package com.folio.online.yahoo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.folio.domain.Amount;
import com.folio.domain.Currency;
import com.folio.domain.Quote;
import com.folio.domain.TickerSymbol;
import org.slf4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static com.folio.domain.Currency.currency;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import org.springframework.stereotype.Component;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public final class QuoteFetcher {

    private static final Logger LOG = getLogger(QuoteFetcher.class);
    private static final String BASE_URL = "https://query1.finance.yahoo.com/v8/finance/chart/";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public QuoteFetcher() {
        this(HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build(),
             new ObjectMapper());
    }

    QuoteFetcher(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = requireNonNull(httpClient);
        this.objectMapper = requireNonNull(objectMapper);
    }

    public Optional<Quote> fetchQuote(TickerSymbol tickerSymbol) {
        requireNonNull(tickerSymbol);
        String url = BASE_URL + tickerSymbol.value();
        LOG.info("Yahoo Finance: GET {}", url);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", USER_AGENT)
                    .header("Accept", "application/json")
                    .timeout(REQUEST_TIMEOUT)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                LOG.warn("Yahoo Finance: HTTP {} for {}", response.statusCode(), url);
                return empty();
            }

            return parseQuote(response.body());
        } catch (Exception e) {
            LOG.warn("Yahoo Finance: fetch failed for {}", url, e);
            return empty();
        }
    }

    private Optional<Quote> parseQuote(String body) throws Exception {
        JsonNode meta = objectMapper.readTree(body)
                .path("chart")
                .path("result")
                .path(0)
                .path("meta");

        if (meta.isMissingNode()) {
            return empty();
        }

        JsonNode priceNode = meta.path("regularMarketPrice");
        JsonNode currencyNode = meta.path("currency");
        JsonNode timeNode = meta.path("regularMarketTime");

        if (priceNode.isMissingNode() || currencyNode.isMissingNode() || timeNode.isMissingNode()) {
            return empty();
        }

        double price = priceNode.asDouble();
        if (price <= 0) return empty();

        String currencyCode = currencyNode.asText().toUpperCase();
        // Yahoo returns ZAc (South African cents) for JSE-listed stocks; 100 ZAc = 1 ZAR
        if ("ZAC".equals(currencyCode)) {
            price = price / 100.0;
            currencyCode = "ZAR";
        }

        Optional<Currency> currency = currency(currencyCode);
        if (currency.isEmpty()) {
            LOG.warn("Yahoo Finance: unknown currency {} in response", currencyNode.asText());
            return empty();
        }

        Amount amount = new Amount(price, currency.get());
        Instant timestamp = Instant.ofEpochSecond(timeNode.asLong());
        return Optional.of(new Quote(amount, timestamp));
    }
}


