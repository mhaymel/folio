package com.folio.quote.sources;

import com.folio.quote.AbstractHtmlQuoteSource;
import com.folio.quote.CsvConfigLoader;
import jakarta.annotation.PostConstruct;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Step 3: Finanzen.net HTML scraping (EUR only).
 * URL paths from finanzennet.csv config file.
 */
@Component
@Order(3)
public class FinanzenNetSource extends AbstractHtmlQuoteSource {

    private static final String BASE_URL = "https://www.finanzen.net/";

    // Look for price in snapshot quote box
    private static final Pattern PRICE_PATTERN = Pattern.compile(
        "(?:snapshot-value-current|intraday-price)[^>]*>\\s*(?:<[^>]*>)*\\s*([0-9]+[.,][0-9]+)");

    // Fallback: meta tag or JSON-LD price
    private static final Pattern META_PRICE = Pattern.compile(
        "\"price\"\\s*:\\s*\"?([0-9]+[.,]?[0-9]*)\"?");

    private Map<String, String> config;

    @PostConstruct
    void init() {
        config = CsvConfigLoader.loadTwoColumn("finanzennet.csv");
        log.info("FinanzenNet: loaded {} ISIN path mappings", config.size());
    }

    @Override
    public String providerName() {
        return "FinanzenNet";
    }

    @Override
    public Optional<Double> fetchQuote(String isin) {
        String path = config.get(isin);
        if (path == null) return Optional.empty();

        String url = BASE_URL + path;
        return fetchHtml(url).flatMap(html -> {
            Matcher m = PRICE_PATTERN.matcher(html);
            if (m.find()) {
                return parseDecimal(m.group(1));
            }
            Matcher mm = META_PRICE.matcher(html);
            if (mm.find()) {
                return parseDecimal(mm.group(1));
            }
            log.debug("FinanzenNet: no price found for {}", isin);
            return Optional.empty();
        });
    }
}

