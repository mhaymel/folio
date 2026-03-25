package com.folio.service;

import com.folio.model.IsinQuote;
import com.folio.model.QuoteProvider;
import com.folio.model.Setting;
import com.folio.quote.IsinsQuoteLoader;
import com.folio.repository.IsinQuoteRepository;
import com.folio.repository.QuoteProviderRepository;
import com.folio.repository.SettingRepository;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class QuoteService {

    private static final Logger log = LoggerFactory.getLogger(QuoteService.class);

    private final IsinsQuoteLoader quoteLoader;
    private final IsinQuoteRepository isinQuoteRepo;
    private final QuoteProviderRepository providerRepo;
    private final SettingRepository settingRepo;
    private final EntityManager em;
    private final AtomicBoolean fetchInProgress = new AtomicBoolean(false);
    private final AtomicLong lastScheduledFetch = new AtomicLong(0);

    public QuoteService(IsinsQuoteLoader quoteLoader, IsinQuoteRepository isinQuoteRepo,
                        QuoteProviderRepository providerRepo, SettingRepository settingRepo,
                        EntityManager em) {
        this.quoteLoader = quoteLoader;
        this.isinQuoteRepo = isinQuoteRepo;
        this.providerRepo = providerRepo;
        this.settingRepo = settingRepo;
        this.em = em;
    }

    /**
     * Scheduled task that runs every minute and checks whether enough time has
     * elapsed since the last fetch (based on the configured interval).
     */
    @Scheduled(fixedDelay = 60_000, initialDelay = 30_000)
    public void scheduledFetch() {
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
     */
    @Transactional
    public int triggerFetch() {
        if (!fetchInProgress.compareAndSet(false, true)) {
            log.info("Quote fetch already in progress, skipping");
            return 0;
        }

        try {
            Set<String> heldIsins = getHeldIsins();
            if (heldIsins.isEmpty()) {
                log.info("No held ISINs to fetch quotes for");
                return 0;
            }

            log.info("Fetching quotes for {} ISINs", heldIsins.size());
            Map<String, IsinsQuoteLoader.QuoteResult> results = quoteLoader.fetchQuotes(heldIsins);

            int upserted = 0;
            for (var entry : results.entrySet()) {
                String isinCode = entry.getKey();
                IsinsQuoteLoader.QuoteResult result = entry.getValue();

                try {
                    upsertQuote(isinCode, result.price(), result.providerName());
                    upserted++;
                } catch (Exception e) {
                    log.error("Failed to persist quote for {}: {}", isinCode, e.getMessage());
                }
            }

            // Update last fetch timestamp
            updateLastFetchTimestamp();
            lastScheduledFetch.set(System.currentTimeMillis());

            log.info("Quote fetch complete: {}/{} ISINs updated", upserted, heldIsins.size());
            return upserted;
        } finally {
            fetchInProgress.set(false);
        }
    }

    private Set<String> getHeldIsins() {
        List<String> isins = em.createQuery(
            "SELECT t.isin.isin FROM Transaction t GROUP BY t.isin.isin HAVING SUM(t.count) > 0",
            String.class
        ).getResultList();
        return new LinkedHashSet<>(isins);
    }

    private void upsertQuote(String isinCode, double price, String providerName) {
        // Find the isin entity ID
        Integer isinId = (Integer) em.createQuery("SELECT i.id FROM Isin i WHERE i.isin = :code")
            .setParameter("code", isinCode)
            .getSingleResult();

        // Find or create the quote provider
        QuoteProvider provider = providerRepo.findByName(providerName)
            .orElseGet(() -> providerRepo.save(QuoteProvider.builder().name(providerName).build()));

        // Upsert: find existing quote for this ISIN or create new
        Optional<IsinQuote> existing = isinQuoteRepo.findByIsinId(isinId);
        if (existing.isPresent()) {
            IsinQuote quote = existing.get();
            quote.setValue(price);
            quote.setQuoteProvider(provider);
            quote.setFetchedAt(LocalDateTime.now());
            isinQuoteRepo.save(quote);
        } else {
            com.folio.model.Isin isin = em.find(com.folio.model.Isin.class, isinId);
            IsinQuote quote = IsinQuote.builder()
                .isin(isin)
                .quoteProvider(provider)
                .value(price)
                .fetchedAt(LocalDateTime.now())
                .build();
            isinQuoteRepo.save(quote);
        }
    }

    private void updateLastFetchTimestamp() {
        Setting setting = settingRepo.findByKey("quote.last.fetch.timestamp")
            .orElseGet(() -> Setting.builder().key("quote.last.fetch.timestamp").value("").build());
        setting.setValue(LocalDateTime.now().toString());
        settingRepo.save(setting);
    }

    private int getIntervalMinutes() {
        return settingRepo.findByKey("quote.fetch.interval.minutes")
            .map(s -> {
                try { return Integer.parseInt(s.getValue()); }
                catch (Exception e) { return 60; }
            })
            .orElse(60);
    }
}

