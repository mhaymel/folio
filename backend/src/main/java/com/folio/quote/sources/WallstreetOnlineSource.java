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
 * Step 10: WallstreetOnline HTML scraping.
 * URL paths from wallstreetonline.csv config file.
 * Uses hardcoded 0.86 factor for USD→EUR conversion.
 */
@Component
@Order(10)
public class WallstreetOnlineSource extends AbstractHtmlQuoteSource {

    private static final String BASE_URL = "https://www.wallstreet-online.de/";
    private static final double USD_TO_EUR_FACTOR = 0.86;

    private static final Pattern PRICE_PATTERN = Pattern.compile(
        "(?:push-data|Kurs|Letzter)[^>]*>\\s*(?:<[^>]*>)*\\s*([0-9]+[.,][0-9]+)",
        Pattern.CASE_INSENSITIVE);

    private static final Pattern CURRENCY_PATTERN = Pattern.compile(
        "(?:USD|\\$)", Pattern.CASE_INSENSITIVE);

    private Map<String, String> config;

    @PostConstruct
    void init() {
        config = CsvConfigLoader.loadTwoColumn("wallstreetonline.csv");
        log.info("WallstreetOnline: loaded {} ISIN path mappings", config.size());
    }

    @Override
    public String providerName() {
        return "WallstreetOnline";
    }

    @Override
    public Optional<Double> fetchQuote(String isin) {
        String path = config.get(isin);
        if (path == null) return Optional.empty();

        String url = BASE_URL + path;
        return fetchHtml(url).flatMap(html -> {
            Matcher m = PRICE_PATTERN.matcher(html);
            if (m.find()) {
                Optional<Double> price = parseDecimal(m.group(1));
                if (price.isPresent()) {
                    // Auto-detect USD: look for USD/$ near the price in the HTML
                    int start = Math.max(0, m.start() - 300);
                    int end = Math.min(html.length(), m.end() + 300);
                    String context = html.substring(start, end);
                    if (CURRENCY_PATTERN.matcher(context).find()) {
                        return Optional.of(price.get() * USD_TO_EUR_FACTOR);
                    }
                    return price;
                }
            }
            log.debug("WallstreetOnline: no price found for {}", isin);
            return Optional.empty();
        });
    }
}

