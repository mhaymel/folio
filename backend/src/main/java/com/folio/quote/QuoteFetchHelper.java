package com.folio.quote;

import static java.lang.Double.parseDouble;

import org.slf4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import static java.util.Optional.empty;
import static java.util.Optional.of;


/**
 * Utility class providing common HTTP fetching and decimal parsing for quote sources.
 */
public final class QuoteFetchHelper {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .connectTimeout(CONNECT_TIMEOUT)
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();

    private QuoteFetchHelper() {
    }

    /**
     * Fetch an HTML page. Returns empty on any HTTP error or non-200 status.
     */
    public static Optional<String> fetchHtml(String url, Logger log, String providerName) {
        log.info("{}: fetching HTML from {}", providerName, url);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", USER_AGENT)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "de-DE,de;q=0.9,en;q=0.8")
                .timeout(REQUEST_TIMEOUT)
                .GET()
                .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return of(response.body());
            }
            log.debug("{}: HTTP {} for {}", providerName, response.statusCode(), url);
            return empty();
        } catch (Exception exception) {
            log.debug("{}: fetch failed for {}: {}", providerName, url, exception.getMessage());
            return empty();
        }
    }

    /**
     * Fetch JSON content from a URL.
     */
    public static Optional<String> fetchJson(String url, Logger log, String providerName) {
        log.info("{}: fetching JSON from {}", providerName, url);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/json")
                .timeout(REQUEST_TIMEOUT)
                .GET()
                .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return of(response.body());
            }
            log.debug("{}: HTTP {} for {}", providerName, response.statusCode(), url);
            return empty();
        } catch (Exception exception) {
            log.debug("{}: fetch failed for {}: {}", providerName, url, exception.getMessage());
            return empty();
        }
    }

    /**
     * Parse a decimal string that may use German format (comma as decimal separator).
     * Strips whitespace, non-breaking spaces, thousands separators.
     */
    public static Optional<Double> parseDecimal(String raw) {
        try {
            String cleaned = raw.trim()
                .replace("\u00a0", "")   // non-breaking space
                .replace(" ", "")
                .replace("&nbsp;", "");

            if (cleaned.contains(",") && cleaned.contains(".")) {
                cleaned = cleaned.replace(".", "").replace(",", ".");
            } else if (cleaned.contains(",")) {
                cleaned = cleaned.replace(",", ".");
            }

            double value = parseDouble(cleaned);
            if (value <= 0 || Double.isNaN(value) || Double.isInfinite(value)) {
                return empty();
            }
            return of(value);
        } catch (Exception exception) {
            return empty();
        }
    }
}

