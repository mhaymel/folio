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
 * Step 6: JustETF REST API retry (same as step 1).
 */
@Component
@Order(6)
final class JustEtfApiRetrySource implements QuoteSource {

    private static final Logger LOG = getLogger(JustEtfApiRetrySource.class);

    private static final String URL_TEMPLATE =
        "https://www.justetf.com/api/etfs/%s/quote?locale=de&currency=EUR";

    private static final Pattern PRICE_PATTERN =
        Pattern.compile("\"(?:latestQuote|rawLatestQuote|quote|lastPrice)\"\\s*:\\s*([0-9]+\\.?[0-9]*)");

    @Override
    public String providerName() {
        return "JustETF";
    }

    @Override
    public Optional<Double> fetchQuote(Isin isin) {
        String url = format(URL_TEMPLATE, isin.value());
        return QuoteFetchHelper.fetchJson(url, LOG, providerName()).flatMap(json -> {
            Matcher matcher = PRICE_PATTERN.matcher(json);
            if (matcher.find()) {
                return QuoteFetchHelper.parseDecimal(matcher.group(1));
            }
            return empty();
        });
    }
}
