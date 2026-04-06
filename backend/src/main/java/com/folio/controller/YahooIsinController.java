package com.folio.controller;

import com.folio.domain.Isin;
import com.folio.domain.TickerSymbol;
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
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/yahoo-isin")
@Tag(name = "Yahoo ISIN", description = "Lookup ticker symbols from Yahoo Finance for held portfolio ISINs")
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
    @Operation(summary = "Fetch ticker symbols from Yahoo Finance for all held ISINs — results are not persisted")
    @Transactional(readOnly = true)
    public ResponseEntity<YahooIsinFetchResult> fetch() {
        Map<String, String> isinToName = loadHeldIsinsWithNames();

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
            Optional<IsinEntity> isinEntity = isinRepository.findByIsin(item.isin());
            if (isinEntity.isEmpty()) continue;

            Optional<TickerSymbolEntity> existingByIsin = tickerSymbolRepository.findByIsin(isinEntity.get());
            if (existingByIsin.isPresent()) {
                existingByIsin.get().setSymbol(item.tickerSymbol());
                updated++;
            } else {
                tickerSymbolRepository.save(new TickerSymbolEntity(null, isinEntity.get(), item.tickerSymbol()));
                created++;
            }
        }

        return ResponseEntity.ok(new YahooIsinSaveResult(created, updated));
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> loadHeldIsinsWithNames() {
        List<Object[]> rows = em.createNativeQuery("""
                SELECT i.isin,
                       (SELECT n.name FROM isin_name n WHERE n.isin_id = i.id ORDER BY n.id ASC LIMIT 1)
                FROM isin i
                WHERE EXISTS (
                    SELECT 1 FROM "transaction" t
                    WHERE t.isin_id = i.id
                    GROUP BY t.isin_id
                    HAVING SUM(t."count") > 0
                )
                ORDER BY i.isin ASC
                """).getResultList();

        Map<String, String> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            result.put((String) row[0], (String) row[1]);
        }
        return result;
    }
}
