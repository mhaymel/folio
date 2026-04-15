package com.folio.controller;

import com.folio.domain.Isin;
import com.folio.dto.YahooIsinDuplicateTickerItem;
import com.folio.dto.YahooIsinFetchResult;
import com.folio.dto.YahooIsinSaveResult;
import com.folio.dto.YahooIsinWithTickerItem;
import com.folio.dto.YahooIsinWithoutTickerItem;
import com.folio.model.IsinEntity;
import com.folio.model.TickerSymbolEntity;
import com.folio.online.yahoo.IsinTickerSearch;
import com.folio.online.yahoo.IsinTickerSearchResult;
import com.folio.repository.IsinRepository;
import com.folio.repository.TickerSymbolRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManager;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@RestController
@RequestMapping("/api/yahoo-ticker-for-isin")
@Tag(name = "Yahoo Ticker for ISIN", description = "Resolve ticker symbols from Yahoo Finance for all known ISINs")
class YahooIsinController {

    private final EntityManager entityManager;
    private final IsinTickerSearch isinTickerSearch;
    private final YahooIsinRepositories repositories;

    public YahooIsinController(EntityManager entityManager, IsinTickerSearch isinTickerSearch,
                               IsinRepository isinRepository, TickerSymbolRepository tickerSymbolRepository) {
        this.entityManager = requireNonNull(entityManager);
        this.isinTickerSearch = requireNonNull(isinTickerSearch);
        this.repositories = new YahooIsinRepositories(requireNonNull(isinRepository), requireNonNull(tickerSymbolRepository));
    }

    @PostMapping("/fetch")
    @Operation(summary = "Fetch ticker symbols from Yahoo Finance for all known ISINs — results are not persisted")
    @Transactional(readOnly = true)
    public ResponseEntity<YahooIsinFetchResult> fetch() {
        Map<String, String> isinToName = loadAllIsinsWithNames();

        Set<Isin> isins = isinToName.keySet().stream()
                .map(Isin::new)
                .collect(Collectors.toSet());

        IsinTickerSearchResult searchResult = isinTickerSearch.search(isins);

        List<YahooIsinWithTickerItem> withTicker = searchResult.found().entrySet().stream()
                .map(entry -> new YahooIsinWithTickerItem(
                        entry.getKey().value(),
                        entry.getValue(),
                        isinToName.get(entry.getKey().value())))
                .sorted(Comparator.comparing(YahooIsinWithTickerItem::isin))
                .toList();

        List<YahooIsinWithoutTickerItem> withoutTicker = searchResult.notFound().stream()
                .map(isin -> new YahooIsinWithoutTickerItem(isin.value(), isinToName.get(isin.value())))
                .sorted(Comparator.comparing(YahooIsinWithoutTickerItem::isin))
                .toList();

        List<YahooIsinDuplicateTickerItem> duplicateTickers = withTicker.stream()
                .collect(Collectors.groupingBy(YahooIsinWithTickerItem::tickerSymbol))
                .entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .flatMap(entry -> entry.getValue().stream()
                        .map(item -> new YahooIsinDuplicateTickerItem(item.isin(), item.tickerSymbol(), item.name())))
                .sorted(Comparator.comparing((YahooIsinDuplicateTickerItem duplicate) -> duplicate.tickerSymbol().value())
                        .thenComparing(YahooIsinDuplicateTickerItem::isin))
                .toList();

        return ResponseEntity.ok(new YahooIsinFetchResult(withTicker, withoutTicker, duplicateTickers));
    }

    @PostMapping("/save")
    @Operation(summary = "Save fetched ticker symbols into the database")
    @Transactional
    public ResponseEntity<YahooIsinSaveResult> save(@RequestBody List<YahooIsinWithTickerItem> items) {
        int created = 0;
        int updated = 0;
        for (YahooIsinWithTickerItem item : items) {
            YahooIsinSaveResult result = saveItem(item);
            created += result.created();
            updated += result.updated();
        }
        return ResponseEntity.ok(new YahooIsinSaveResult(created, updated));
    }

    private YahooIsinSaveResult saveItem(YahooIsinWithTickerItem item) {
        Optional<IsinEntity> isinEntityOpt = repositories.isinRepository().findByIsin(item.isin());
        if (isinEntityOpt.isEmpty()) return new YahooIsinSaveResult(0, 0);
        IsinEntity isinEntity = isinEntityOpt.get();

        String symbol = item.tickerSymbol().value();
        TickerSymbolEntity tickerSymbol = repositories.tickerSymbolRepository().findBySymbol(symbol)
            .orElseGet(() -> repositories.tickerSymbolRepository().save(new TickerSymbolEntity(null, symbol)));

        Long exactMatch = (Long) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM isin_ticker WHERE isin_id = :isinId AND ticker_symbol_id = :tsId")
            .setParameter("isinId", isinEntity.getId())
            .setParameter("tsId", tickerSymbol.getId())
            .getSingleResult();
        if (exactMatch > 0) return new YahooIsinSaveResult(0, 0);

        List<?> existingLink = entityManager.createNativeQuery(
                "SELECT id FROM isin_ticker WHERE isin_id = :isinId")
            .setParameter("isinId", isinEntity.getId())
            .getResultList();

        if (!existingLink.isEmpty()) {
            entityManager.createNativeQuery(
                    "UPDATE isin_ticker SET ticker_symbol_id = :tsId WHERE isin_id = :isinId")
                .setParameter("tsId", tickerSymbol.getId())
                .setParameter("isinId", isinEntity.getId())
                .executeUpdate();
            return new YahooIsinSaveResult(0, 1);
        }
        entityManager.createNativeQuery(
                "INSERT INTO isin_ticker (isin_id, ticker_symbol_id) VALUES (:isinId, :tsId)")
            .setParameter("isinId", isinEntity.getId())
            .setParameter("tsId", tickerSymbol.getId())
            .executeUpdate();
        return new YahooIsinSaveResult(1, 0);
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> loadAllIsinsWithNames() {
        List<Object[]> rows = entityManager.createNativeQuery("""
                SELECT i.isin,
                       (SELECT n.name FROM isin_name n WHERE n.isin_id = i.id ORDER BY n.id ASC LIMIT 1)
                FROM isin i
                ORDER BY i.isin ASC
                """).getResultList();

        Map<String, String> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            result.put((String) row[0], (String) row[1]);
        }
        return result;
    }
}
