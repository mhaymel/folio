package com.folio.quote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Cascade fallback quote loader. Tries each source in order;
 * ISINs successfully resolved are removed before the next source is attempted.
 */
@Component
public class IsinsQuoteLoader {

    private static final Logger log = LoggerFactory.getLogger(IsinsQuoteLoader.class);

    private final List<QuoteSource> sources;

    /**
     * Sources are injected in @Order priority (1=first, 10=last).
     */
    public IsinsQuoteLoader(List<QuoteSource> sources) {
        this.sources = sources;
        log.info("IsinsQuoteLoader initialized with {} sources: {}",
            sources.size(),
            sources.stream().map(s -> s.providerName() + "(" + s.getClass().getSimpleName() + ")").toList());
    }

    /**
     * Fetch EUR quotes for a set of ISINs using cascade fallback.
     *
     * @param isins set of ISIN codes to fetch
     * @return map of ISIN → QuoteResult (price + provider name)
     */
    public Map<String, QuoteResult> fetchQuotes(Set<String> isins) {
        Map<String, QuoteResult> results = new HashMap<>();
        Set<String> remaining = new LinkedHashSet<>(isins);

        for (QuoteSource source : sources) {
            if (remaining.isEmpty()) break;

            log.debug("Trying source {} for {} remaining ISINs", source.providerName(), remaining.size());
            Set<String> resolved = new HashSet<>();

            for (String isin : remaining) {
                try {
                    Optional<Double> price = source.fetchQuote(isin);
                    if (price.isPresent()) {
                        results.put(isin, new QuoteResult(price.get(), source.providerName()));
                        resolved.add(isin);
                        log.debug("{}: {} → {}", source.providerName(), isin, String.format("%.2f", price.get()));
                    }
                } catch (Exception e) {
                    log.debug("{}: error fetching {}: {}", source.providerName(), isin, e.getMessage());
                }
            }

            remaining.removeAll(resolved);
            if (!resolved.isEmpty()) {
                log.info("{}: resolved {} ISINs, {} remaining",
                    source.providerName(), resolved.size(), remaining.size());
            }
        }

        if (!remaining.isEmpty()) {
            log.warn("No quote found for {} ISINs: {}", remaining.size(),
                remaining.size() <= 10 ? remaining : remaining.stream().limit(10).toList() + "...");
        }

        log.info("Quote fetch complete: {}/{} ISINs resolved", results.size(), isins.size());
        return results;
    }

    /**
     * Result of a successful quote fetch.
     */
    public record QuoteResult(double price, String providerName) {}
}

