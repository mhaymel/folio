package com.folio.quote.sources;

import com.folio.quote.AbstractHtmlQuoteSource;
import com.folio.quote.EcbExchangeRateProvider;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Step 8: FondsDiscount.de USD quote → EUR conversion.
 * Same URL as step 7, but looking for USD-denominated prices.
 */
@Component
@Order(8)
public class FondsDiscountUsdSource extends AbstractHtmlQuoteSource {

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
    public Optional<Double> fetchQuote(String isin) {
        String url = String.format(URL_TEMPLATE, isin);
        return fetchHtml(url).flatMap(html -> {
            Matcher m = USD_PRICE_PATTERN.matcher(html);
            if (m.find()) {
                Optional<Double> usdPrice = parseDecimal(m.group(1));
                if (usdPrice.isPresent()) {
                    return Optional.of(ecb.usdToEur(usdPrice.get()));
                }
            }
            log.debug("FondsDiscount USD: no price found for {}", isin);
            return Optional.empty();
        });
    }
}

