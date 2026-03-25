package com.folio.quote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

/**
 * Base class for quote sources that scrape HTML pages.
 * Provides common HTTP fetching and decimal parsing utilities.
 */
public abstract class AbstractHtmlQuoteSource implements QuoteSource {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();

    /**
     * Fetch an HTML page. Returns empty on any HTTP error or non-200 status.
     */
    protected Optional<String> fetchHtml(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "de-DE,de;q=0.9,en;q=0.8")
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return Optional.of(response.body());
            }
            log.debug("{}: HTTP {} for {}", providerName(), response.statusCode(), url);
            return Optional.empty();
        } catch (Exception e) {
            log.debug("{}: fetch failed for {}: {}", providerName(), url, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Fetch JSON content from a URL.
     */
    protected Optional<String> fetchJson(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return Optional.of(response.body());
            }
            log.debug("{}: HTTP {} for {}", providerName(), response.statusCode(), url);
            return Optional.empty();
        } catch (Exception e) {
            log.debug("{}: fetch failed for {}: {}", providerName(), url, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Parse a decimal string that may use German format (comma as decimal separator).
     * Strips whitespace, non-breaking spaces, thousands separators.
     */
    protected Optional<Double> parseDecimal(String raw) {
        try {
            String s = raw.trim()
                .replace("\u00a0", "")   // non-breaking space
                .replace(" ", "")
                .replace("&nbsp;", "");

            if (s.contains(",") && s.contains(".")) {
                // German format: 1.234,56
                s = s.replace(".", "").replace(",", ".");
            } else if (s.contains(",")) {
                s = s.replace(",", ".");
            }

            double value = Double.parseDouble(s);
            if (value <= 0 || Double.isNaN(value) || Double.isInfinite(value)) {
                return Optional.empty();
            }
            return Optional.of(value);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}

