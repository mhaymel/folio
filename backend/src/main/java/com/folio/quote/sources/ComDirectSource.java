package com.folio.quote.sources;

import com.folio.quote.AbstractHtmlQuoteSource;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Step 9: ComDirect HTML scraping.
 * URL: https://www.comdirect.de/inf/zertifikate/{ISIN}
 */
@Component
@Order(9)
public class ComDirectSource extends AbstractHtmlQuoteSource {

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
            log.debug("ComDirect: no price found for {}", isin);
            return Optional.empty();
        });
    }
}

