package com.folio.quote.sources;

import com.folio.domain.Isin;
import com.folio.quote.CsvConfigLoader;
import com.folio.quote.QuoteFetchHelper;
import com.folio.quote.QuoteSource;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import static java.util.Optional.empty;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Step 3: Finanzen.net HTML scraping (EUR only).
 * URL paths from finanzennet.csv config file.
 */
@Component
@Order(3)
final class FinanzenNetSource implements QuoteSource {

    private static final Logger log = getLogger(FinanzenNetSource.class);

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
    public Optional<Double> fetchQuote(Isin isin) {
        String path = config.get(isin.value());
        if (path == null) return empty();

        String url = BASE_URL + path;
        return QuoteFetchHelper.fetchHtml(url, log, providerName()).flatMap(html -> {
            Matcher m = PRICE_PATTERN.matcher(html);
            if (m.find()) {
                return QuoteFetchHelper.parseDecimal(m.group(1));
            }
            Matcher mm = META_PRICE.matcher(html);
            if (mm.find()) {
                return QuoteFetchHelper.parseDecimal(mm.group(1));
            }
            log.debug("FinanzenNet: no price found for {}", isin);
            return empty();
        });
    }
}
