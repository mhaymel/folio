package com.folio.quote.sources;

import com.folio.quote.AbstractHtmlQuoteSource;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Step 5: JustETF HTML scraping.
 * URL: https://www.justetf.com/at/etf-profile.html?isin={ISIN}
 */
@Component
@Order(5)
public class JustEtfHtmlSource extends AbstractHtmlQuoteSource {

    private static final String URL_TEMPLATE =
        "https://www.justetf.com/at/etf-profile.html?isin=%s";

    // JustETF HTML contains the price in various formats
    private static final Pattern PRICE_PATTERN = Pattern.compile(
        "(?:infobox-value|cur-val|quote-val|val)[^>]*>\\s*(?:<[^>]*>)*\\s*([0-9]+[.,][0-9]+)");

    private static final Pattern JSON_PRICE = Pattern.compile(
        "\"(?:latestQuote|quote|lastPrice)\"\\s*:\\s*([0-9]+\\.?[0-9]*)");

    @Override
    public String providerName() {
        return "JustETF";
    }

    @Override
    public Optional<Double> fetchQuote(String isin) {
        String url = String.format(URL_TEMPLATE, isin);
        return fetchHtml(url).flatMap(html -> {
            Matcher jm = JSON_PRICE.matcher(html);
            if (jm.find()) {
                return parseDecimal(jm.group(1));
            }
            Matcher m = PRICE_PATTERN.matcher(html);
            if (m.find()) {
                return parseDecimal(m.group(1));
            }
            log.debug("JustETF HTML: no price found for {}", isin);
            return Optional.empty();
        });
    }
}

