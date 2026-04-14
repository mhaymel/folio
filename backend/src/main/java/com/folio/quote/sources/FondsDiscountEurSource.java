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
 * Step 7: FondsDiscount.de EUR quote.
 * URL: https://www.fondsdiscount.de/fonds/etf/{ISIN}/
 */
@Component
@Order(7)
final class FondsDiscountEurSource implements QuoteSource {

    private static final Logger log = getLogger(FondsDiscountEurSource.class);

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
    public Optional<Double> fetchQuote(Isin isin) {
        String url = format(URL_TEMPLATE, isin.value());
        return QuoteFetchHelper.fetchHtml(url, log, providerName()).flatMap(html -> {
            Matcher m = PRICE_PATTERN.matcher(html);
            if (m.find()) {
                return QuoteFetchHelper.parseDecimal(m.group(1));
            }
            Matcher gm = GENERIC_EUR_PRICE.matcher(html);
            if (gm.find()) {
                return QuoteFetchHelper.parseDecimal(gm.group(1));
            }
            log.debug("FondsDiscount EUR: no price found for {}", isin);
            return empty();
        });
    }
}
