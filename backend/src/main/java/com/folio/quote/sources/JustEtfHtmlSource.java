package com.folio.quote.sources;

import com.folio.domain.Isin;

import static java.lang.String.format;
import com.folio.quote.QuoteFetchHelper;
import com.folio.quote.QuoteSource;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;
import static java.util.Optional.empty;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Step 5: JustETF HTML scraping.
 * URL: https://www.justetf.com/at/etf-profile.html?isin={ISIN}
 */
@Component
@Order(5)
final class JustEtfHtmlSource implements QuoteSource {

    private static final Logger LOG = getLogger(JustEtfHtmlSource.class);

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
    public Optional<Double> fetchQuote(Isin isin) {
        String url = format(URL_TEMPLATE, isin.value());
        return QuoteFetchHelper.fetchHtml(url, LOG, providerName()).flatMap(html -> {
            Matcher jsonMatcher = JSON_PRICE.matcher(html);
            if (jsonMatcher.find()) {
                return QuoteFetchHelper.parseDecimal(jsonMatcher.group(1));
            }
            Matcher matcher = PRICE_PATTERN.matcher(html);
            if (matcher.find()) {
                return QuoteFetchHelper.parseDecimal(matcher.group(1));
            }
            LOG.debug("JustETF HTML: no price found for {}", isin);
            return empty();
        });
    }
}
