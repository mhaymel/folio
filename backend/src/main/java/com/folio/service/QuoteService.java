package com.folio.service;

import com.folio.domain.Isin;
import com.folio.domain.IsinFactory;
import com.folio.model.IsinEntity;
import com.folio.model.IsinQuoteData;
import com.folio.model.IsinQuoteEntity;
import com.folio.model.IsinQuoteSource;
import com.folio.model.QuoteProviderEntity;
import com.folio.model.SettingEntity;
import com.folio.quote.IsinsQuoteLoader;
import com.folio.quote.QuoteResult;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;
import static org.slf4j.LoggerFactory.getLogger;

import static java.util.Objects.requireNonNull;

public final class QuoteService {

    private static final Logger LOG = getLogger(QuoteService.class);
    private static final int DEFAULT_INTERVAL_MINUTES = 60;
    private static final long SCHEDULE_CHECK_DELAY_MS = 60_000;
    private static final long SCHEDULE_INITIAL_DELAY_MS = 30_000;
    private static final long MS_PER_MINUTE = 60_000L;

    private final IsinsQuoteLoader quoteLoader;
    private final QuoteDataAccess dataAccess;
    private final FetchState fetchState;

    QuoteService(IsinsQuoteLoader quoteLoader, QuoteDataAccess dataAccess, FetchState fetchState) {
        this.quoteLoader = requireNonNull(quoteLoader);
        this.dataAccess = requireNonNull(dataAccess);
        this.fetchState = requireNonNull(fetchState);
    }


    /**
     * Scheduled task that runs every minute and checks whether enough time has
     * elapsed since the last fetch (based on the configured interval).
     */
    @Scheduled(fixedDelay = SCHEDULE_CHECK_DELAY_MS, initialDelay = SCHEDULE_INITIAL_DELAY_MS)
    public void scheduledFetch() {
        if (!isEnabled()) {
            return; // quote fetching is turned off
        }

        int intervalMinutes = intervalMinutes();
        long intervalMs = intervalMinutes * MS_PER_MINUTE;
        long elapsed = System.currentTimeMillis() - fetchState.lastScheduledFetch().get();

        if (elapsed < intervalMs) {
            return; // not yet time
        }

        LOG.info("Scheduled quote fetch triggered (interval: {} min)", intervalMinutes);
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
        if (!fetchState.inProgress().compareAndSet(false, true)) {
            LOG.info("Quote fetch already in progress, skipping");
            return 0;
        }

        try {
            // 1. Short read-only transaction: collect held ISINs
            Set<Isin> heldIsins = dataAccess.transactionTemplate().execute(status -> findHeldIsins());
            if (heldIsins == null || heldIsins.isEmpty()) {
                LOG.info("No held ISINs to fetch quotes for");
                return 0;
            }

            // 2. No transaction: slow HTTP calls to external quote providers
            LOG.info("Fetching quotes for {} ISINs", heldIsins.size());
            Map<Isin, QuoteResult> results = quoteLoader.fetchQuotes(heldIsins);

            // 3. Short write transaction: persist all fetched quotes
            Integer upserted = dataAccess.transactionTemplate().execute(status -> {
                int count = 0;
                for (var entry : results.entrySet()) {
                    Isin isin = entry.getKey();
                    QuoteResult result = entry.getValue();
                    try {
                        upsertQuote(isin.value(), result.price(), result.providerName());
                        count++;
                    } catch (Exception exception) {
                        LOG.error("Failed to persist quote for {}: {}", isin, exception.getMessage());
                    }
                }
                updateLastFetchTimestamp();
                return count;
            });

            fetchState.lastScheduledFetch().set(System.currentTimeMillis());

            LOG.info("Quote fetch complete: {}/{} ISINs updated", upserted, heldIsins.size());
            return upserted != null ? upserted : 0;
        } finally {
            fetchState.inProgress().set(false);
        }
    }

    private Set<Isin> findHeldIsins() {
        List<String> isins = dataAccess.entityManager().createQuery(
            "SELECT t.context.isin.isin FROM TransactionEntity t GROUP BY t.context.isin.isin HAVING SUM(t.values.count) > 0",
            String.class
        ).getResultList();
        return isins.stream()
            .flatMap(isinCode -> IsinFactory.of(isinCode).stream())
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void upsertQuote(String isinCode, double price, String providerName) {
        Integer isinId = (Integer) dataAccess.entityManager().createQuery("SELECT i.id FROM IsinEntity i WHERE i.isin = :code")
            .setParameter("code", isinCode)
            .getSingleResult();

        QuoteProviderEntity provider = dataAccess.repos().provider().findByName(providerName)
            .orElseGet(() -> dataAccess.repos().provider().save(new QuoteProviderEntity(null, providerName)));

        Optional<IsinQuoteEntity> existing = dataAccess.repos().isinQuote().findBySource_IsinId(isinId);
        if (existing.isPresent()) {
            IsinQuoteEntity quote = existing.get();
            quote.setValue(price);
            quote.setQuoteProvider(provider);
            quote.setFetchedAt(LocalDateTime.now());
            dataAccess.repos().isinQuote().save(quote);
        } else {
            IsinEntity isin = dataAccess.entityManager().find(IsinEntity.class, isinId);
            IsinQuoteEntity quote = new IsinQuoteEntity(null,
                new IsinQuoteSource(isin, provider),
                new IsinQuoteData(price, LocalDateTime.now(), null));
            dataAccess.repos().isinQuote().save(quote);
        }
    }

    private void updateLastFetchTimestamp() {
        SettingEntity setting = dataAccess.repos().setting().findByKey("quote.last.fetch.timestamp")
            .orElseGet(() -> new SettingEntity(null, "quote.last.fetch.timestamp", ""));
        setting.setValue(LocalDateTime.now().toString());
        dataAccess.repos().setting().save(setting);
    }

    private int intervalMinutes() {
        return dataAccess.repos().setting().findByKey("quote.fetch.interval.minutes")
            .map(setting -> parseIntervalOrDefault(setting.getValue()))
            .orElse(DEFAULT_INTERVAL_MINUTES);
    }

    private int parseIntervalOrDefault(String value) {
        try {
            return parseInt(value);
        } catch (NumberFormatException exception) {
            LOG.warn("Invalid interval minutes value '{}', using default", value);
            return DEFAULT_INTERVAL_MINUTES;
        }
    }

    private boolean isEnabled() {
        return dataAccess.repos().setting().findByKey("quote.fetch.enabled")
            .map(setting -> Boolean.parseBoolean(setting.getValue()))
            .orElse(false);
    }
}

