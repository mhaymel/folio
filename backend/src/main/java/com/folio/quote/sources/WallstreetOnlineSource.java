package com.folio.quote.sources;

import static java.lang.Math.min;

import static java.lang.Math.max;

import com.folio.domain.IsinCode;
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
import static java.util.Optional.of;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Step 10: WallstreetOnline HTML scraping.
 * URL paths from wallstreetonline.csv config file.
 * Uses hardcoded 0.86 factor for USD→EUR conversion.
 */
@Component
@Order(10)
public final class WallstreetOnlineSource implements QuoteSource {

    private static final Logger log = getLogger(WallstreetOnlineSource.class);

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
    public Optional<Double> fetchQuote(IsinCode isin) {
        String path = config.get(isin.value());
        if (path == null) return empty();

        String url = BASE_URL + path;
        return QuoteFetchHelper.fetchHtml(url, log, providerName()).flatMap(html -> {
            Matcher m = PRICE_PATTERN.matcher(html);
            if (m.find()) {
                Optional<Double> price = QuoteFetchHelper.parseDecimal(m.group(1));
                if (price.isPresent()) {
                    // Auto-detect USD: look for USD/$ near the price in the HTML
                    int start = max(0, m.start() - 300);
                    int end = min(html.length(), m.end() + 300);
                    String context = html.substring(start, end);
                    if (CURRENCY_PATTERN.matcher(context).find()) {
                        return of(price.get() * USD_TO_EUR_FACTOR);
                    }
                    return price;
                }
            }
            log.debug("WallstreetOnline: no price found for {}", isin);
            return empty();
        });
    }
}
