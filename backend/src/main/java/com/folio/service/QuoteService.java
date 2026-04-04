package com.folio.service;

import static java.lang.Integer.parseInt;

import com.folio.domain.Isin;
import com.folio.model.IsinQuoteEntity;
import com.folio.model.QuoteProviderEntity;
import com.folio.model.SettingEntity;
import com.folio.quote.IsinsQuoteLoader;
import com.folio.repository.QuoteRepositories;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Service
public final class QuoteService {

    private static final Logger log = getLogger(QuoteService.class);

    private final IsinsQuoteLoader quoteLoader;
    private final QuoteRepositories repos;
    private final EntityManager em;
    private final TransactionTemplate tx;
    private final AtomicBoolean fetchInProgress = new AtomicBoolean(false);
    private final AtomicLong lastScheduledFetch = new AtomicLong(0);

    public QuoteService(IsinsQuoteLoader quoteLoader, QuoteRepositories repos, EntityManager em,
                        TransactionTemplate tx) {
        this.quoteLoader = quoteLoader;
        this.repos = repos;
        this.em = em;
        this.tx = tx;
    }

    /**
     * Scheduled task that runs every minute and checks whether enough time has
     * elapsed since the last fetch (based on the configured interval).
     */
    @Scheduled(fixedDelay = 60_000, initialDelay = 30_000)
    public void scheduledFetch() {
        if (!isEnabled()) {
            return; // quote fetching is turned off
        }

        int intervalMinutes = getIntervalMinutes();
        long intervalMs = intervalMinutes * 60_000L;
        long elapsed = System.currentTimeMillis() - lastScheduledFetch.get();

        if (elapsed < intervalMs) {
            return; // not yet time
        }

        log.info("Scheduled quote fetch triggered (interval: {} min)", intervalMinutes);
        triggerFetch();
    }

    /**
     * Trigger an immediate quote fetch for all held ISINs.
     * Returns the number of successfully fetched quotes.
     *
     * <p>The method is intentionally NOT {@code @Transactional} so that the
     * (potentially slow) HTTP quote fetches do not hold a database transaction
     * open, which would block concurrent writes to the {@code isin} table.</p>
     */
    public int triggerFetch() {
        if (!fetchInProgress.compareAndSet(false, true)) {
            log.info("Quote fetch already in progress, skipping");
            return 0;
        }

        try {
            // 1. Short read-only transaction: collect held ISINs
            Set<Isin> heldIsins = tx.execute(status -> getHeldIsins());
            if (heldIsins == null || heldIsins.isEmpty()) {
                log.info("No held ISINs to fetch quotes for");
                return 0;
            }

            // 2. No transaction: slow HTTP calls to external quote providers
            log.info("Fetching quotes for {} ISINs", heldIsins.size());
            Map<Isin, IsinsQuoteLoader.QuoteResult> results = quoteLoader.fetchQuotes(heldIsins);

            // 3. Short write transaction: persist all fetched quotes
            Integer upserted = tx.execute(status -> {
                int count = 0;
                for (var entry : results.entrySet()) {
                    Isin isin = entry.getKey();
                    IsinsQuoteLoader.QuoteResult result = entry.getValue();
                    try {
                        upsertQuote(isin.value(), result.price(), result.providerName());
                        count++;
                    } catch (Exception e) {
                        log.error("Failed to persist quote for {}: {}", isin, e.getMessage());
                    }
                }
                updateLastFetchTimestamp();
                return count;
            });

            lastScheduledFetch.set(System.currentTimeMillis());

            log.info("Quote fetch complete: {}/{} ISINs updated", upserted, heldIsins.size());
            return upserted != null ? upserted : 0;
        } finally {
            fetchInProgress.set(false);
        }
    }

    private Set<Isin> getHeldIsins() {
        List<String> isins = em.createQuery(
            "SELECT t.isin.isin FROM TransactionEntity t GROUP BY t.isin.isin HAVING SUM(t.count) > 0",
            String.class
        ).getResultList();
        return isins.stream()
            .flatMap(s -> Isin.of(s).stream())
            .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private void upsertQuote(String isinCode, double price, String providerName) {
        Integer isinId = (Integer) em.createQuery("SELECT i.id FROM IsinEntity i WHERE i.isin = :code")
            .setParameter("code", isinCode)
            .getSingleResult();

        QuoteProviderEntity provider = repos.provider().findByName(providerName)
            .orElseGet(() -> repos.provider().save(QuoteProviderEntity.builder().name(providerName).build()));

        Optional<IsinQuoteEntity> existing = repos.isinQuote().findByIsinId(isinId);
        if (existing.isPresent()) {
            IsinQuoteEntity quote = existing.get();
            quote.setValue(price);
            quote.setQuoteProvider(provider);
            quote.setFetchedAt(LocalDateTime.now());
            repos.isinQuote().save(quote);
        } else {
            com.folio.model.IsinEntity isin = em.find(com.folio.model.IsinEntity.class, isinId);
            IsinQuoteEntity quote = IsinQuoteEntity.builder()
                .isin(isin)
                .quoteProvider(provider)
                .value(price)
                .fetchedAt(LocalDateTime.now())
                .build();
            repos.isinQuote().save(quote);
        }
    }

    private void updateLastFetchTimestamp() {
        SettingEntity setting = repos.setting().findByKey("quote.last.fetch.timestamp")
            .orElseGet(() -> SettingEntity.builder().key("quote.last.fetch.timestamp").value("").build());
        setting.setValue(LocalDateTime.now().toString());
        repos.setting().save(setting);
    }

    private int getIntervalMinutes() {
        return repos.setting().findByKey("quote.fetch.interval.minutes")
            .map(s -> {
                try { return parseInt(s.getValue()); }
                catch (Exception e) { return 60; }
            })
            .orElse(60);
    }

    private boolean isEnabled() {
        return repos.setting().findByKey("quote.fetch.enabled")
            .map(s -> Boolean.parseBoolean(s.getValue()))
            .orElse(false);
    }
}

