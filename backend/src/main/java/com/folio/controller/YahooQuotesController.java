package com.folio.controller;

import com.folio.domain.Isin;
import com.folio.domain.TickerSymbol;
import com.folio.dto.ExportColumn;
import com.folio.dto.ExportRequest;
import com.folio.dto.PaginatedResponseDto;
import com.folio.dto.YahooFetchResultDto;
import com.folio.dto.YahooQuoteWithQuoteDto;
import com.folio.dto.YahooQuoteWithoutQuoteDto;
import com.folio.model.IsinEntity;
import com.folio.model.IsinQuoteEntity;
import com.folio.model.QuoteProviderEntity;
import com.folio.online.yahoo.QuoteFetcher;
import com.folio.repository.IsinRepository;
import com.folio.repository.QuoteRepositories;
import com.folio.service.ExportService;
import com.folio.service.PaginationHelper;
import com.folio.service.SortHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

@RestController
@RequestMapping("/api/yahoo-quotes")
@Tag(name = "Yahoo Quotes", description = "Yahoo Finance quote status for held portfolio ISINs")
public class YahooQuotesController {

    private static final Logger log = getLogger(YahooQuotesController.class);
    private static final String PROVIDER_NAME = "Yahoo Finance";

    private static final Map<String, Comparator<YahooQuoteWithQuoteDto>> WITH_QUOTE_SORT = Map.of(
        "isin", SortHelper.text(d -> d.getIsin() == null ? null : d.getIsin().value()),
        "name", SortHelper.text(YahooQuoteWithQuoteDto::getName),
        "tickerSymbol", SortHelper.text(YahooQuoteWithQuoteDto::getTickerSymbol),
        "price", SortHelper.number(YahooQuoteWithQuoteDto::getPrice),
        "currency", SortHelper.text(YahooQuoteWithQuoteDto::getCurrency),
        "provider", SortHelper.text(YahooQuoteWithQuoteDto::getProvider),
        "fetchedAt", SortHelper.comparing(YahooQuoteWithQuoteDto::getRawFetchedAt)
    );

    private static final Map<String, Comparator<YahooQuoteWithoutQuoteDto>> WITHOUT_QUOTE_SORT = Map.of(
        "isin", SortHelper.text(d -> d.getIsin() == null ? null : d.getIsin().value()),
        "name", SortHelper.text(YahooQuoteWithoutQuoteDto::getName),
        "tickerSymbol", SortHelper.text(YahooQuoteWithoutQuoteDto::getTickerSymbol)
    );

    private final EntityManager em;
    private final ExportService exportService;
    private final IsinRepository isinRepository;
    private final QuoteRepositories quoteRepos;
    private final QuoteFetcher quoteFetcher;

    public YahooQuotesController(EntityManager em, ExportService exportService,
                                  IsinRepository isinRepository,
                                  QuoteRepositories quoteRepos,
                                  QuoteFetcher quoteFetcher) {
        this.em = em;
        this.exportService = exportService;
        this.isinRepository = isinRepository;
        this.quoteRepos = quoteRepos;
        this.quoteFetcher = quoteFetcher;
    }

    @PostMapping("/fetch")
    @Operation(summary = "Fetch Yahoo Finance quotes for all held ISINs that have a ticker symbol")
    @Transactional
    public ResponseEntity<YahooFetchResultDto> fetch() {
        List<Object[]> rows = loadHeldIsinsWithTicker();
        int total = rows.size();
        int fetched = 0;
        int noTicker = 0;
        int noQuote = 0;

        for (Object[] row : rows) {
            YahooFetchResultDto r = processRow(row);
            fetched += r.fetched();
            noTicker += r.noTicker();
            noQuote += r.noQuote();
        }

        log.info("Yahoo quote fetch complete: total={} fetched={} noTicker={} noQuote={}",
            total, fetched, noTicker, noQuote);
        return ResponseEntity.ok(new YahooFetchResultDto(total, fetched, noTicker, noQuote));
    }

    @GetMapping("/with-quote")
    @Operation(summary = "Get held ISINs that have a current quote")
    @Transactional(readOnly = true)
    public ResponseEntity<PaginatedResponseDto<YahooQuoteWithQuoteDto>> getWithQuote(
            @RequestParam(defaultValue = "isin") String sortField,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String isin,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String ticker,
            @RequestParam(required = false) String currency) {
        List<YahooQuoteWithQuoteDto> data = filterWithQuote(loadWithQuote(), isin, name, ticker, currency);
        data = SortHelper.sort(data, sortField, sortDir, WITH_QUOTE_SORT);
        return ResponseEntity.ok(PaginationHelper.paginate(data, page, pageSize));
    }

    @GetMapping("/with-quote/export")
    @Operation(summary = "Export held ISINs with quote as CSV or Excel")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> exportWithQuote(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) String sortField,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String isin,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String ticker,
            @RequestParam(required = false) String currency) {
        List<YahooQuoteWithQuoteDto> data = filterWithQuote(loadWithQuote(), isin, name, ticker, currency);
        if (sortField != null && !sortField.isBlank()) {
            data = SortHelper.sort(data, sortField, sortDir, WITH_QUOTE_SORT);
        }
        List<ExportColumn<YahooQuoteWithQuoteDto>> columns = List.of(
            new ExportColumn<>("ISIN", YahooQuoteWithQuoteDto::getIsin),
            new ExportColumn<>("Name", YahooQuoteWithQuoteDto::getName),
            new ExportColumn<>("Ticker", YahooQuoteWithQuoteDto::getTickerSymbol),
            new ExportColumn<>("Price", YahooQuoteWithQuoteDto::getPrice),
            new ExportColumn<>("Currency", YahooQuoteWithQuoteDto::getCurrency),
            new ExportColumn<>("Provider", YahooQuoteWithQuoteDto::getProvider),
            new ExportColumn<>("Fetched At", YahooQuoteWithQuoteDto::getFetchedAt)
        );
        return exportService.export(new ExportRequest<>(data, columns, format, "quotes-with-quote"));
    }

    @GetMapping("/without-quote")
    @Operation(summary = "Get held ISINs that have no quote")
    @Transactional(readOnly = true)
    public ResponseEntity<PaginatedResponseDto<YahooQuoteWithoutQuoteDto>> getWithoutQuote(
            @RequestParam(defaultValue = "isin") String sortField,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String isin,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String ticker) {
        List<YahooQuoteWithoutQuoteDto> data = filterWithoutQuote(loadWithoutQuote(), isin, name, ticker);
        data = SortHelper.sort(data, sortField, sortDir, WITHOUT_QUOTE_SORT);
        return ResponseEntity.ok(PaginationHelper.paginate(data, page, pageSize));
    }

    @GetMapping("/without-quote/export")
    @Operation(summary = "Export held ISINs without quote as CSV or Excel")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> exportWithoutQuote(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) String sortField,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String isin,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String ticker) {
        List<YahooQuoteWithoutQuoteDto> data = filterWithoutQuote(loadWithoutQuote(), isin, name, ticker);
        if (sortField != null && !sortField.isBlank()) {
            data = SortHelper.sort(data, sortField, sortDir, WITHOUT_QUOTE_SORT);
        }
        List<ExportColumn<YahooQuoteWithoutQuoteDto>> columns = List.of(
            new ExportColumn<>("ISIN", YahooQuoteWithoutQuoteDto::getIsin),
            new ExportColumn<>("Name", YahooQuoteWithoutQuoteDto::getName),
            new ExportColumn<>("Ticker", YahooQuoteWithoutQuoteDto::getTickerSymbol)
        );
        return exportService.export(new ExportRequest<>(data, columns, format, "quotes-without-quote"));
    }

    private List<YahooQuoteWithQuoteDto> filterWithQuote(List<YahooQuoteWithQuoteDto> data,
            String isin, String name, String ticker, String currency) {
        return data.stream()
            .filter(d -> matches(d.getIsin() != null ? d.getIsin().value() : null, isin))
            .filter(d -> matches(d.getName(), name))
            .filter(d -> matches(d.getTickerSymbol(), ticker))
            .filter(d -> matches(d.getCurrency(), currency))
            .toList();
    }

    private List<YahooQuoteWithoutQuoteDto> filterWithoutQuote(List<YahooQuoteWithoutQuoteDto> data,
            String isin, String name, String ticker) {
        return data.stream()
            .filter(d -> matches(d.getIsin() != null ? d.getIsin().value() : null, isin))
            .filter(d -> matches(d.getName(), name))
            .filter(d -> matches(d.getTickerSymbol(), ticker))
            .toList();
    }

    private boolean matches(String value, String filter) {
        if (filter == null || filter.isBlank()) return true;
        if (value == null) return false;
        return value.toLowerCase(Locale.ROOT).contains(filter.toLowerCase(Locale.ROOT));
    }

    private YahooFetchResultDto processRow(Object[] row) {
        String isinCode = (String) row[0];
        String ticker = (String) row[1];
        if (ticker == null || ticker.isBlank()) return new YahooFetchResultDto(0, 0, 1, 0);
        Optional<com.folio.domain.Quote> quote = quoteFetcher.fetchQuote(new TickerSymbol(ticker));
        if (quote.isEmpty()) {
            log.debug("Yahoo Finance: no quote for ticker {} (ISIN {})", ticker, isinCode);
            return new YahooFetchResultDto(0, 0, 0, 1);
        }
        upsertQuote(isinCode, quote.get().amount().value(), quote.get().amount().currency().name());
        return new YahooFetchResultDto(0, 1, 0, 0);
    }

    private void upsertQuote(String isinCode, double price, String currency) {
        IsinEntity isin = isinRepository.findByIsin(isinCode).orElse(null);
        if (isin == null) return;

        QuoteProviderEntity provider = quoteRepos.provider().findByName(PROVIDER_NAME)
            .orElseGet(() -> quoteRepos.provider().save(
                QuoteProviderEntity.builder().name(PROVIDER_NAME).build()
            ));

        Optional<IsinQuoteEntity> existing = quoteRepos.isinQuote().findByIsinId(isin.getId());
        if (existing.isPresent()) {
            IsinQuoteEntity q = existing.get();
            q.setValue(price);
            q.setCurrency(currency);
            q.setQuoteProvider(provider);
            q.setFetchedAt(LocalDateTime.now());
            quoteRepos.isinQuote().save(q);
        } else {
            quoteRepos.isinQuote().save(IsinQuoteEntity.builder()
                .isin(isin)
                .quoteProvider(provider)
                .value(price)
                .currency(currency)
                .fetchedAt(LocalDateTime.now())
                .build());
        }
    }

    @SuppressWarnings("unchecked")
    private List<Object[]> loadHeldIsinsWithTicker() {
        return em.createNativeQuery("""
            SELECT i.isin,
                   (SELECT ts.symbol FROM isin_ticker it2
                    JOIN ticker_symbol ts ON ts.id = it2.ticker_symbol_id
                    WHERE it2.isin_id = i.id ORDER BY it2.ticker_symbol_id ASC LIMIT 1)
            FROM isin i
            WHERE EXISTS (
                SELECT 1 FROM "transaction" t
                WHERE t.isin_id = i.id
                GROUP BY t.isin_id
                HAVING SUM(t."count") > 0
            )
            ORDER BY i.isin ASC
            """).getResultList();
    }

    @SuppressWarnings("unchecked")
    private List<YahooQuoteWithQuoteDto> loadWithQuote() {
        List<Object[]> rows = em.createNativeQuery("""
            SELECT i.isin,
                   (SELECT n.name FROM isin_name n WHERE n.isin_id = i.id ORDER BY n.id ASC LIMIT 1),
                   (SELECT ts.symbol FROM isin_ticker it2
                    JOIN ticker_symbol ts ON ts.id = it2.ticker_symbol_id
                    WHERE it2.isin_id = i.id ORDER BY it2.ticker_symbol_id ASC LIMIT 1),
                   q."value",
                   q.currency,
                   qp.name,
                   q.fetched_at
            FROM isin_quote q
            JOIN isin i ON i.id = q.isin_id
            JOIN quote_provider qp ON qp.id = q.quote_provider_id
            WHERE EXISTS (
                SELECT 1 FROM "transaction" t
                WHERE t.isin_id = i.id
                GROUP BY t.isin_id
                HAVING SUM(t."count") > 0
            )
            ORDER BY i.isin ASC
            """).getResultList();

        return rows.stream()
            .map(r -> YahooQuoteWithQuoteDto.builder()
                .isin(new Isin((String) r[0]))
                .name((String) r[1])
                .tickerSymbol((String) r[2])
                .price(r[3] != null ? ((Number) r[3]).doubleValue() : null)
                .currency((String) r[4])
                .provider((String) r[5])
                .fetchedAt(r[6] != null ? ((java.sql.Timestamp) r[6]).toLocalDateTime() : null)
                .build())
            .toList();
    }

    @SuppressWarnings("unchecked")
    private List<YahooQuoteWithoutQuoteDto> loadWithoutQuote() {
        List<Object[]> rows = em.createNativeQuery("""
            SELECT i.isin,
                   (SELECT n.name FROM isin_name n WHERE n.isin_id = i.id ORDER BY n.id ASC LIMIT 1),
                   (SELECT ts.symbol FROM isin_ticker it2
                    JOIN ticker_symbol ts ON ts.id = it2.ticker_symbol_id
                    WHERE it2.isin_id = i.id ORDER BY it2.ticker_symbol_id ASC LIMIT 1)
            FROM isin i
            WHERE NOT EXISTS (SELECT 1 FROM isin_quote q WHERE q.isin_id = i.id)
            AND EXISTS (
                SELECT 1 FROM "transaction" t
                WHERE t.isin_id = i.id
                GROUP BY t.isin_id
                HAVING SUM(t."count") > 0
            )
            ORDER BY i.isin ASC
            """).getResultList();

        return rows.stream()
            .map(r -> YahooQuoteWithoutQuoteDto.builder()
                .isin(new Isin((String) r[0]))
                .name((String) r[1])
                .tickerSymbol((String) r[2])
                .build())
            .toList();
    }
}
