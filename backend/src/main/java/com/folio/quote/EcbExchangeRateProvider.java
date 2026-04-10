package com.folio.quote;

import static java.lang.Double.parseDouble;
import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.util.Objects.requireNonNull;

/**
 * Fetches the EUR/USD exchange rate from the ECB XML feed.
 * Falls back to a static rate on failure. Caches for 1 hour.
 */
@Component
public final class EcbExchangeRateProvider {

    private static final Logger log = getLogger(EcbExchangeRateProvider.class);
    private static final String ECB_URL = "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";
    private static final double FALLBACK_EUR_USD = 0.92;

    private final RestTemplate restTemplate;
    private final ConcurrentMap<String, CachedRate> cache;

    public EcbExchangeRateProvider() {
        this(new RestTemplate(), new ConcurrentHashMap<>());
    }

    EcbExchangeRateProvider(RestTemplate restTemplate, ConcurrentMap<String, CachedRate> cache) {
        this.restTemplate = requireNonNull(restTemplate);
        this.cache = requireNonNull(cache);
    }

    public double getUsdToEurRate() {
        CachedRate cached = cache.get("USD");
        if (cached != null && !cached.isExpired()) {
            return cached.rate();
        }

        try {
            String xml = restTemplate.getForObject(ECB_URL, String.class);
            if (xml != null) {
                // Parse: <Cube currency='USD' rate='1.0837'/>
                int idx = xml.indexOf("currency='USD'");
                if (idx > 0) {
                    int rateStart = xml.indexOf("rate='", idx) + 6;
                    int rateEnd = xml.indexOf("'", rateStart);
                    double eurPerUsd = parseDouble(xml.substring(rateStart, rateEnd));
                    double usdToEur = 1.0 / eurPerUsd;
                    cache.put("USD", new CachedRate(usdToEur));
                    log.info("ECB EUR/USD rate fetched: 1 USD = {} EUR", format("%.4f", usdToEur));
                    return usdToEur;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch ECB exchange rate, using fallback {}: {}", FALLBACK_EUR_USD, e.getMessage());
        }
        return FALLBACK_EUR_USD;
    }

    /**
     * Convert a USD price to EUR.
     */
    public double usdToEur(double usdPrice) {
        return usdPrice * getUsdToEurRate();
    }
}
