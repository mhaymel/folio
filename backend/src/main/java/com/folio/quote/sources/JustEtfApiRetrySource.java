package com.folio.quote.sources;

import com.folio.quote.AbstractHtmlQuoteSource;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Step 6: JustETF REST API retry (same as step 1).
 */
@Component
@Order(6)
public class JustEtfApiRetrySource extends AbstractHtmlQuoteSource {

    private static final String URL_TEMPLATE =
        "https://www.justetf.com/api/etfs/%s/quote?locale=de&currency=EUR";

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
            return Optional.empty();
        });
    }
}

