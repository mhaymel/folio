package com.folio.controller;

import com.folio.domain.Isin;
import com.folio.dto.*;
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

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/yahoo-ticker-for-isin")
@Tag(name = "Yahoo Ticker for ISIN", description = "Resolve ticker symbols from Yahoo Finance for all known ISINs")
public class YahooIsinController {

    private final EntityManager em;
    private final IsinTickerSearch isinTickerSearch;
    private final IsinRepository isinRepository;
    private final TickerSymbolRepository tickerSymbolRepository;

    public YahooIsinController(EntityManager em, IsinTickerSearch isinTickerSearch,
                               IsinRepository isinRepository, TickerSymbolRepository tickerSymbolRepository) {
        this.em = em;
        this.isinTickerSearch = isinTickerSearch;
        this.isinRepository = isinRepository;
        this.tickerSymbolRepository = tickerSymbolRepository;
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
                .map(e -> new YahooIsinWithTickerItem(
                        e.getKey().value(),
                        e.getValue().value(),
                        isinToName.get(e.getKey().value())))
                .sorted(Comparator.comparing(YahooIsinWithTickerItem::isin))
                .toList();

        List<YahooIsinWithoutTickerItem> withoutTicker = searchResult.notFound().stream()
                .map(isin -> new YahooIsinWithoutTickerItem(isin.value(), isinToName.get(isin.value())))
                .sorted(Comparator.comparing(YahooIsinWithoutTickerItem::isin))
                .toList();

        // Ticker symbols that appear for more than one ISIN in this fetch result
        List<YahooIsinDuplicateTickerItem> duplicateTickers = withTicker.stream()
                .collect(Collectors.groupingBy(YahooIsinWithTickerItem::tickerSymbol))
                .entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .flatMap(e -> e.getValue().stream()
                        .map(i -> new YahooIsinDuplicateTickerItem(i.isin(), i.tickerSymbol(), i.name())))
                .sorted(Comparator.comparing(YahooIsinDuplicateTickerItem::tickerSymbol)
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
            YahooIsinSaveResult r = saveItem(item);
            created += r.created();
            updated += r.updated();
        }
        return ResponseEntity.ok(new YahooIsinSaveResult(created, updated));
    }

    private YahooIsinSaveResult saveItem(YahooIsinWithTickerItem item) {
        Optional<IsinEntity> isinEntityOpt = isinRepository.findByIsin(item.isin());
        if (isinEntityOpt.isEmpty()) return new YahooIsinSaveResult(0, 0);
        IsinEntity isinEntity = isinEntityOpt.get();

        TickerSymbolEntity tickerSymbol = tickerSymbolRepository.findBySymbol(item.tickerSymbol())
            .orElseGet(() -> tickerSymbolRepository.save(new TickerSymbolEntity(null, item.tickerSymbol())));

        Long exactMatch = (Long) em.createNativeQuery(
                "SELECT COUNT(*) FROM isin_ticker WHERE isin_id = :isinId AND ticker_symbol_id = :tsId")
            .setParameter("isinId", isinEntity.getId())
            .setParameter("tsId", tickerSymbol.getId())
            .getSingleResult();
        if (exactMatch > 0) return new YahooIsinSaveResult(0, 0);

        List<?> existingLink = em.createNativeQuery(
                "SELECT id FROM isin_ticker WHERE isin_id = :isinId")
            .setParameter("isinId", isinEntity.getId())
            .getResultList();

        if (!existingLink.isEmpty()) {
            em.createNativeQuery(
                    "UPDATE isin_ticker SET ticker_symbol_id = :tsId WHERE isin_id = :isinId")
                .setParameter("tsId", tickerSymbol.getId())
                .setParameter("isinId", isinEntity.getId())
                .executeUpdate();
            return new YahooIsinSaveResult(0, 1);
        }
        em.createNativeQuery(
                "INSERT INTO isin_ticker (isin_id, ticker_symbol_id) VALUES (:isinId, :tsId)")
            .setParameter("isinId", isinEntity.getId())
            .setParameter("tsId", tickerSymbol.getId())
            .executeUpdate();
        return new YahooIsinSaveResult(1, 0);
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> loadAllIsinsWithNames() {
        List<Object[]> rows = em.createNativeQuery("""
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
