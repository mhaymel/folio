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

import static java.lang.String.format;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Step 9: ComDirect HTML scraping.
 * URL: https://www.comdirect.de/inf/zertifikate/{ISIN}
 */
@Component
@Order(9)
public final class ComDirectSource implements QuoteSource {

    private static final Logger log = getLogger(ComDirectSource.class);

    private static final String URL_TEMPLATE = "https://www.comdirect.de/inf/zertifikate/%s";

    private static final Pattern PRICE_PATTERN = Pattern.compile(
        "(?:realtime-indicator|price|Kurs)[^>]*>\\s*(?:<[^>]*>)*\\s*([0-9]+[.,][0-9]+)",
        Pattern.CASE_INSENSITIVE);

    private static final Pattern JSON_PRICE = Pattern.compile(
        "\"price\"\\s*:\\s*\"?([0-9]+[.,]?[0-9]*)\"?");

    @Override
    public String providerName() {
        return "ComDirect";
    }

    @Override
    public Optional<Double> fetchQuote(Isin isin) {
        String url = format(URL_TEMPLATE, isin.value());
        return QuoteFetchHelper.fetchHtml(url, log, providerName()).flatMap(html -> {
            Matcher jm = JSON_PRICE.matcher(html);
            if (jm.find()) {
                return QuoteFetchHelper.parseDecimal(jm.group(1));
            }
            Matcher m = PRICE_PATTERN.matcher(html);
            if (m.find()) {
                return QuoteFetchHelper.parseDecimal(m.group(1));
            }
            log.debug("ComDirect: no price found for {}", isin);
            return empty();
        });
    }
}
