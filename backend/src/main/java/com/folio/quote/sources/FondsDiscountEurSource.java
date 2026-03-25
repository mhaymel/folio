package com.folio.quote.sources;

import com.folio.quote.AbstractHtmlQuoteSource;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Step 7: FondsDiscount.de EUR quote.
 * URL: https://www.fondsdiscount.de/fonds/etf/{ISIN}/
 */
@Component
@Order(7)
public class FondsDiscountEurSource extends AbstractHtmlQuoteSource {

    private static final String URL_TEMPLATE = "https://www.fondsdiscount.de/fonds/etf/%s/";

    private static final Pattern PRICE_PATTERN = Pattern.compile(
        "(?:Kurs|Rücknahmepreis|NAV|Ausgabekurs)[^>]*>\\s*(?:<[^>]*>)*\\s*([0-9]+[.,][0-9]+)\\s*(?:EUR|€)",
        Pattern.CASE_INSENSITIVE);

    private static final Pattern GENERIC_EUR_PRICE = Pattern.compile(
        "([0-9]+[.,][0-9]+)\\s*(?:EUR|€)");

    @Override
    public String providerName() {
        return "FondsDiscount";
    }

    @Override
    public Optional<Double> fetchQuote(String isin) {
        String url = String.format(URL_TEMPLATE, isin);
        return fetchHtml(url).flatMap(html -> {
            Matcher m = PRICE_PATTERN.matcher(html);
            if (m.find()) {
                return parseDecimal(m.group(1));
            }
            Matcher gm = GENERIC_EUR_PRICE.matcher(html);
            if (gm.find()) {
                return parseDecimal(gm.group(1));
            }
            log.debug("FondsDiscount EUR: no price found for {}", isin);
            return Optional.empty();
        });
    }
}

