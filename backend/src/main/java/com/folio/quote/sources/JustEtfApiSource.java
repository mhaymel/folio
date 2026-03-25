package com.folio.quote.sources;

import com.folio.quote.AbstractHtmlQuoteSource;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Step 1 & 6: JustETF REST API.
 * URL: https://www.justetf.com/api/etfs/{ISIN}/quote?locale=de&currency=EUR
 * Returns JSON with EUR price.
 */
@Component
@Order(1)
public class JustEtfApiSource extends AbstractHtmlQuoteSource {

    private static final String URL_TEMPLATE =
        "https://www.justetf.com/api/etfs/%s/quote?locale=de&currency=EUR";

    // Match "latestQuote" or "rawLatestQuote" or just a "quote" field with numeric value
    private static final Pattern PRICE_PATTERN =
        Pattern.compile("\"(?:latestQuote|rawLatestQuote|quote|lastPrice)\"\\s*:\\s*([0-9]+\\.?[0-9]*)");

    @Override
    public String providerName() {
        return "JustETF";
    }

    @Override
    public Optional<Double> fetchQuote(String isin) {
        String url = String.format(URL_TEMPLATE, isin);
        return fetchJson(url).flatMap(json -> {
            Matcher m = PRICE_PATTERN.matcher(json);
            if (m.find()) {
                return parseDecimal(m.group(1));
            }
            log.debug("JustETF API: no price found in response for {}", isin);
            return Optional.empty();
        });
    }
}

