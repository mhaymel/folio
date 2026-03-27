package com.folio.quote.sources;

import com.folio.domain.IsinCode;
import static java.lang.String.format;
import com.folio.quote.CsvConfigLoader;
import com.folio.quote.EcbExchangeRateProvider;
import static java.lang.String.format;
import com.folio.quote.QuoteFetchHelper;
import com.folio.quote.QuoteSource;
import static java.lang.String.format;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;
import org.springframework.core.annotation.Order;
import static java.lang.String.format;
import org.springframework.stereotype.Component;

import java.util.Map;
import static java.lang.String.format;
import java.util.Optional;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.util.regex.Matcher;
import static java.lang.String.format;
import java.util.regex.Pattern;

/**
 * Step 4: CNBC HTML scraping.
 * Uses ticker symbols from isin.symbol.csv. Prices in USD, converted to EUR.
 */
@Component
@Order(4)
public final class CnbcSource implements QuoteSource {

    private static final Logger log = getLogger(CnbcSource.class);

    private static final String URL_TEMPLATE = "https://www.cnbc.com/quotes/%s";

    // CNBC embeds price in structured data or specific elements
    private static final Pattern PRICE_PATTERN = Pattern.compile(
        "\"(?:price|last)\"\\s*:\\s*\"?([0-9]+\\.?[0-9]*)\"?");

    private static final Pattern HTML_PRICE = Pattern.compile(
        "QuoteStrip-lastPrice[^>]*>\\s*(?:<[^>]*>)*\\s*\\$?([0-9]+[.,]?[0-9]*)");

    private final EcbExchangeRateProvider ecb;
    private Map<String, String> config;

    public CnbcSource(EcbExchangeRateProvider ecb) {
        this.ecb = ecb;
    }

    @PostConstruct
    void init() {
        config = CsvConfigLoader.loadThreeColumnSecond("isin.symbol.csv");
        log.info("CNBC: loaded {} ISIN→ticker mappings", config.size());
    }

    @Override
    public String providerName() {
        return "CNBC";
    }

    @Override
    public Optional<Double> fetchQuote(IsinCode isin) {
        String ticker = config.get(isin.value());
        if (ticker == null) return empty();

        String url = format(URL_TEMPLATE, ticker);
        return QuoteFetchHelper.fetchHtml(url, log, providerName()).flatMap(html -> {
            // Try JSON/structured data
            Matcher m = PRICE_PATTERN.matcher(html);
            if (m.find()) {
                Optional<Double> usdPrice = QuoteFetchHelper.parseDecimal(m.group(1));
                if (usdPrice.isPresent()) {
                    return of(ecb.usdToEur(usdPrice.get()));
                }
            }
            // Try HTML pattern
            Matcher hm = HTML_PRICE.matcher(html);
            if (hm.find()) {
                Optional<Double> usdPrice = QuoteFetchHelper.parseDecimal(hm.group(1));
                if (usdPrice.isPresent()) {
                    return of(ecb.usdToEur(usdPrice.get()));
                }
            }
            log.debug("CNBC: no price found for {} ({})", isin, ticker);
            return empty();
        });
    }
}
