package com.folio.quote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Fetches the EUR/USD exchange rate from the ECB XML feed.
 * Falls back to a static rate on failure. Caches for 1 hour.
 */
@Component
public class EcbExchangeRateProvider {

    private static final Logger log = LoggerFactory.getLogger(EcbExchangeRateProvider.class);
    private static final String ECB_URL = "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";
    private static final double FALLBACK_EUR_USD = 0.92;

    private final RestTemplate restTemplate;
    private final ConcurrentMap<String, CachedRate> cache = new ConcurrentHashMap<>();

    public EcbExchangeRateProvider() {
        this.restTemplate = new RestTemplate();
    }

    public double getUsdToEurRate() {
        CachedRate cached = cache.get("USD");
        if (cached != null && !cached.isExpired()) {
            return cached.rate;
        }

        try {
            String xml = restTemplate.getForObject(ECB_URL, String.class);
            if (xml != null) {
                // Parse: <Cube currency='USD' rate='1.0837'/>
                int idx = xml.indexOf("currency='USD'");
                if (idx > 0) {
                    int rateStart = xml.indexOf("rate='", idx) + 6;
                    int rateEnd = xml.indexOf("'", rateStart);
                    double eurPerUsd = Double.parseDouble(xml.substring(rateStart, rateEnd));
                    double usdToEur = 1.0 / eurPerUsd;
                    cache.put("USD", new CachedRate(usdToEur));
                    log.info("ECB EUR/USD rate fetched: 1 USD = {} EUR", String.format("%.4f", usdToEur));
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

    private static class CachedRate {
        final double rate;
        final long timestamp;

        CachedRate(double rate) {
            this.rate = rate;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > 3_600_000; // 1 hour
        }
    }
}

