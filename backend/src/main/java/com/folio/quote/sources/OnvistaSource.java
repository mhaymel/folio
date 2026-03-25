package com.folio.quote.sources;

import com.folio.quote.AbstractHtmlQuoteSource;
import com.folio.quote.CsvConfigLoader;
import com.folio.quote.EcbExchangeRateProvider;
import jakarta.annotation.PostConstruct;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Step 2: Onvista HTML scraping.
 * URL paths from onvista.csv config file.
 */
@Component
@Order(2)
public class OnvistaSource extends AbstractHtmlQuoteSource {

    private static final String BASE_URL = "https://www.onvista.de/";

    // Look for price patterns in Onvista HTML: spans with price-like content
    private static final Pattern PRICE_PATTERN = Pattern.compile(
        "(?:Kurs|Geldkurs|Briefkurs|Letzter)[^>]*>\\s*(?:<[^>]*>)*\\s*([0-9]+[.,][0-9]+)",
        Pattern.CASE_INSENSITIVE);

    // Simpler fallback: look for data attributes or JSON-LD with price
    private static final Pattern JSON_PRICE_PATTERN = Pattern.compile(
        "\"price\"\\s*:\\s*\"?([0-9]+[.,]?[0-9]*)\"?");

    private static final Pattern CURRENCY_PATTERN = Pattern.compile(
        "(?:USD|\\$)", Pattern.CASE_INSENSITIVE);

    private final EcbExchangeRateProvider ecb;
    private Map<String, String> config;

    public OnvistaSource(EcbExchangeRateProvider ecb) {
        this.ecb = ecb;
    }

    @PostConstruct
    void init() {
        config = CsvConfigLoader.loadTwoColumn("onvista.csv");
        log.info("Onvista: loaded {} ISIN path mappings", config.size());
    }

    @Override
    public String providerName() {
        return "Onvista";
    }

    @Override
    public Optional<Double> fetchQuote(String isin) {
        String path = config.get(isin);
        if (path == null) return Optional.empty();

        String url = BASE_URL + path;
        return fetchHtml(url).flatMap(html -> {
            // Try JSON price first
            Matcher jm = JSON_PRICE_PATTERN.matcher(html);
            if (jm.find()) {
                Optional<Double> price = parseDecimal(jm.group(1));
                if (price.isPresent()) {
                    // Check if page indicates USD
                    if (CURRENCY_PATTERN.matcher(html.substring(
                            Math.max(0, jm.start() - 200),
                            Math.min(html.length(), jm.end() + 200))).find()) {
                        return Optional.of(ecb.usdToEur(price.get()));
                    }
                    return price;
                }
            }

            // Try HTML pattern
            Matcher m = PRICE_PATTERN.matcher(html);
            if (m.find()) {
                return parseDecimal(m.group(1));
            }

            log.debug("Onvista: no price found for {}", isin);
            return Optional.empty();
        });
    }
}

