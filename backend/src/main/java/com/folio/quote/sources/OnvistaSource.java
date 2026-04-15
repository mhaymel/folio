package com.folio.quote.sources;

import static java.lang.Math.min;

import static java.lang.Math.max;

import com.folio.domain.Isin;
import com.folio.quote.CsvConfigLoader;
import com.folio.quote.EcbExchangeRateProvider;
import com.folio.quote.QuoteFetchHelper;
import com.folio.quote.QuoteSource;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import static java.util.Objects.requireNonNull;
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
 * Step 2: Onvista HTML scraping.
 * URL paths from onvista.csv config file.
 */
@Component
@Order(2)
final class OnvistaSource implements QuoteSource {

    private static final Logger LOG = getLogger(OnvistaSource.class);

    private static final String BASE_URL = "https://www.onvista.de/";

    private static final Pattern PRICE_PATTERN = Pattern.compile(
        "(?:Kurs|Geldkurs|Briefkurs|Letzter)[^>]*>\\s*(?:<[^>]*>)*\\s*([0-9]+[.,][0-9]+)",
        Pattern.CASE_INSENSITIVE);

    private static final Pattern JSON_PRICE_PATTERN = Pattern.compile(
        "\"price\"\\s*:\\s*\"?([0-9]+[.,]?[0-9]*)\"?");

    private static final Pattern CURRENCY_PATTERN = Pattern.compile(
        "(?:USD|\\$)", Pattern.CASE_INSENSITIVE);

    private final EcbExchangeRateProvider ecb;
    private Map<String, String> config;

    public OnvistaSource(EcbExchangeRateProvider ecb) {
        this.ecb = requireNonNull(ecb);
    }

    @PostConstruct
    void init() {
        config = CsvConfigLoader.loadTwoColumn("onvista.csv");
        LOG.info("Onvista: loaded {} ISIN path mappings", config.size());
    }

    @Override
    public String providerName() {
        return "Onvista";
    }

    @Override
    public Optional<Double> fetchQuote(Isin isin) {
        String path = config.get(isin.value());
        if (path == null) return empty();

        String url = BASE_URL + path;
        return QuoteFetchHelper.fetchHtml(url, LOG, providerName()).flatMap(html -> {
            Matcher jsonMatcher = JSON_PRICE_PATTERN.matcher(html);
            if (jsonMatcher.find()) {
                Optional<Double> price = QuoteFetchHelper.parseDecimal(jsonMatcher.group(1));
                if (price.isPresent()) {
                    if (CURRENCY_PATTERN.matcher(html.substring(
                            max(0, jsonMatcher.start() - 200),
                            min(html.length(), jsonMatcher.end() + 200))).find()) {
                        return of(ecb.usdToEur(price.get()));
                    }
                    return price;
                }
            }

            Matcher matcher = PRICE_PATTERN.matcher(html);
            if (matcher.find()) {
                return QuoteFetchHelper.parseDecimal(matcher.group(1));
            }

            LOG.debug("Onvista: no price found for {}", isin);
            return empty();
        });
    }
}
