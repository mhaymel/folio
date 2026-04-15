package com.folio.quote.sources;

import com.folio.domain.Isin;

import static java.lang.String.format;
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
 * Step 4: CNBC HTML scraping.
 * Uses ticker symbols from isin.symbol.csv. Prices in USD, converted to EUR.
 */
@Component
@Order(4)
final class CnbcSource implements QuoteSource {

    private static final Logger LOG = getLogger(CnbcSource.class);

    private static final String URL_TEMPLATE = "https://www.cnbc.com/quotes/%s";

    private static final Pattern PRICE_PATTERN = Pattern.compile(
        "\"(?:price|last)\"\\s*:\\s*\"?([0-9]+\\.?[0-9]*)\"?");

    private static final Pattern HTML_PRICE = Pattern.compile(
        "QuoteStrip-lastPrice[^>]*>\\s*(?:<[^>]*>)*\\s*\\$?([0-9]+[.,]?[0-9]*)");

    private final EcbExchangeRateProvider ecb;
    private Map<String, String> config;

    public CnbcSource(EcbExchangeRateProvider ecb) {
        this.ecb = requireNonNull(ecb);
    }

    @PostConstruct
    void init() {
        config = CsvConfigLoader.loadThreeColumnSecond("isin.symbol.csv");
        LOG.info("CNBC: loaded {} ISIN→ticker mappings", config.size());
    }

    @Override
    public String providerName() {
        return "CNBC";
    }

    @Override
    public Optional<Double> fetchQuote(Isin isin) {
        String ticker = config.get(isin.value());
        if (ticker == null) return empty();

        String url = format(URL_TEMPLATE, ticker);
        return QuoteFetchHelper.fetchHtml(url, LOG, providerName()).flatMap(html -> {
            Matcher matcher = PRICE_PATTERN.matcher(html);
            if (matcher.find()) {
                Optional<Double> usdPrice = QuoteFetchHelper.parseDecimal(matcher.group(1));
                if (usdPrice.isPresent()) {
                    return of(ecb.usdToEur(usdPrice.get()));
                }
            }
            Matcher htmlMatcher = HTML_PRICE.matcher(html);
            if (htmlMatcher.find()) {
                Optional<Double> usdPrice = QuoteFetchHelper.parseDecimal(htmlMatcher.group(1));
                if (usdPrice.isPresent()) {
                    return of(ecb.usdToEur(usdPrice.get()));
                }
            }
            LOG.debug("CNBC: no price found for {} ({})", isin, ticker);
            return empty();
        });
    }
}
