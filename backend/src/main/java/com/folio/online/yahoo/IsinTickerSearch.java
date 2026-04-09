package com.folio.online.yahoo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.folio.domain.Isin;
import com.folio.domain.TickerSymbol;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Resolves ISIN codes to Yahoo Finance ticker symbols via the Yahoo Finance search API.
 */
@Component
public final class IsinTickerSearch {

    private static final Logger LOG = getLogger(IsinTickerSearch.class);
    private static final String BASE_URL = "https://query2.finance.yahoo.com/v1/finance/search";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public IsinTickerSearch() {
        this(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build(),
             new ObjectMapper());
    }

    IsinTickerSearch(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = requireNonNull(httpClient);
        this.objectMapper = requireNonNull(objectMapper);
    }

    /**
     * Searches Yahoo Finance for a ticker symbol for each of the given ISINs.
     * One HTTP request is made per ISIN.
     *
     * @param isins set of ISIN codes to resolve
     * @return result containing resolved pairs and the set of unresolved ISINs
     */
    public IsinTickerSearchResult search(Set<Isin> isins) {
        requireNonNull(isins);

        Map<Isin, TickerSymbol> found = new HashMap<>();
        Set<Isin> notFound = new HashSet<>();

        for (Isin isin : isins) {
            TickerSymbol ticker = fetchTicker(isin);
            if (ticker != null) {
                found.put(isin, ticker);
            } else {
                notFound.add(isin);
            }
        }

        return new IsinTickerSearchResult(found, notFound);
    }

    private TickerSymbol fetchTicker(Isin isin) {
        String url = BASE_URL + "?q=" + URLEncoder.encode(isin.value(), UTF_8)
                + "&quotesCount=5&newsCount=0&enableFuzzyQuery=false";
        LOG.info("Yahoo search: GET {}", url);
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
                LOG.warn("Yahoo search: HTTP {} for ISIN {}", response.statusCode(), isin);
                return null;
            }

            return parseFirstTicker(response.body(), isin);
        } catch (Exception e) {
            LOG.warn("Yahoo search: request failed for ISIN {}", isin, e);
            return null;
        }
    }

    private TickerSymbol parseFirstTicker(String body, Isin isin) throws Exception {
        JsonNode quotes = objectMapper.readTree(body).path("quotes");
        if (!quotes.isArray()) return null;

        for (JsonNode quote : quotes) {
            TickerSymbol ticker = extractTicker(quote, isin);
            if (ticker != null) return ticker;
        }

        LOG.debug("Yahoo search: no ticker found for ISIN {}", isin);
        return null;
    }

    private TickerSymbol extractTicker(JsonNode quote, Isin isin) {
        if (!quote.path("isYahooFinance").asBoolean(false)) return null;
        JsonNode symbolNode = quote.path("symbol");
        if (symbolNode.isMissingNode() || symbolNode.isNull()) return null;
        LOG.debug("Yahoo search: ISIN {} → {}", isin, symbolNode.asText());
        return new TickerSymbol(symbolNode.asText());
    }
}
