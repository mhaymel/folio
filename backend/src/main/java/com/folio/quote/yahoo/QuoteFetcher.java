package com.folio.quote.yahoo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.folio.domain.Amount;
import com.folio.domain.Currency;
import com.folio.domain.Quote;
import com.folio.domain.TickerSymbol;
import com.folio.model.CurrencyEntity;
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
import static org.slf4j.LoggerFactory.getLogger;

public final class QuoteFetcher {

    private static final Logger LOG = getLogger(QuoteFetcher.class);
    private static final String BASE_URL = "https://query1.finance.yahoo.com/v8/finance/chart/";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public QuoteFetcher() {
        this(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build(),
             new ObjectMapper());
    }

    QuoteFetcher(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = requireNonNull(httpClient);
        this.objectMapper = requireNonNull(objectMapper);
    }

    public Optional<Quote> fetchQuote(TickerSymbol tickerCode) {
        requireNonNull(tickerCode);
        String url = BASE_URL + tickerCode.value();
        LOG.info("Yahoo Finance: GET {}", url);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(15))
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

        Optional<Currency> currency = currency(currencyNode.asText());
        if (currency.isEmpty()) {
            LOG.warn("Yahoo Finance: unknown currency {} in response", currencyNode.asText());
            return empty();
        }

        Amount amount = new Amount(price, currency.get());
        Instant timestamp = Instant.ofEpochSecond(timeNode.asLong());
        return Optional.of(new Quote(amount, timestamp));
    }
}


