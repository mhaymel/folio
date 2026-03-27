package com.folio.quote.sources;

import com.folio.domain.IsinCode;
import static java.lang.String.format;
import com.folio.quote.EcbExchangeRateProvider;
import com.folio.quote.QuoteFetchHelper;
import static java.lang.String.format;
import com.folio.quote.QuoteSource;
import org.slf4j.Logger;
import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;
import org.springframework.core.annotation.Order;
import static java.lang.String.format;
import org.springframework.stereotype.Component;

import java.util.Optional;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import static java.lang.String.format;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Step 8: FondsDiscount.de USD quote → EUR conversion.
 * Same URL as step 7, but looking for USD-denominated prices.
 */
@Component
@Order(8)
public final class FondsDiscountUsdSource implements QuoteSource {

    private static final Logger log = getLogger(FondsDiscountUsdSource.class);

    private static final String URL_TEMPLATE = "https://www.fondsdiscount.de/fonds/etf/%s/";

    private static final Pattern USD_PRICE_PATTERN = Pattern.compile(
        "([0-9]+[.,][0-9]+)\\s*(?:USD|\\$)");

    private final EcbExchangeRateProvider ecb;

    public FondsDiscountUsdSource(EcbExchangeRateProvider ecb) {
        this.ecb = ecb;
    }

    @Override
    public String providerName() {
        return "FondsDiscount";
    }

    @Override
    public Optional<Double> fetchQuote(IsinCode isin) {
        String url = format(URL_TEMPLATE, isin.value());
        return QuoteFetchHelper.fetchHtml(url, log, providerName()).flatMap(html -> {
            Matcher m = USD_PRICE_PATTERN.matcher(html);
            if (m.find()) {
                Optional<Double> usdPrice = QuoteFetchHelper.parseDecimal(m.group(1));
                if (usdPrice.isPresent()) {
                    return of(ecb.usdToEur(usdPrice.get()));
                }
            }
            log.debug("FondsDiscount USD: no price found for {}", isin);
            return empty();
        });
    }
}
