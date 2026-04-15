package com.folio.controller;

import com.folio.dto.ExportRequest;
import com.folio.dto.PaginatedResponseDto;
import com.folio.dto.TickerSymbolDto;
import com.folio.service.SortHelper;
import com.folio.service.SortRequest;
import com.folio.dto.ExportColumn;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.folio.domain.Isin;
import jakarta.persistence.EntityManager;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@RestController
@RequestMapping("/api/ticker-symbols")
@Tag(name = "Ticker Symbols", description = "ISIN to ticker symbol mappings")
class TickerSymbolController {

    private static final Map<String, Comparator<TickerSymbolDto>> SORT_FIELDS = Map.of(
        "isin", SortHelper.text(dto -> dto.getIsin() == null ? null : dto.getIsin().value()),
        "tickerSymbol", SortHelper.text(TickerSymbolDto::getTickerSymbol),
        "name", SortHelper.text(TickerSymbolDto::getName)
    );

    private final EntityManager entityManager;
    private final ListOperations listOperations;

    public TickerSymbolController(EntityManager entityManager, ListOperations listOperations) {
        this.entityManager = requireNonNull(entityManager);
        this.listOperations = requireNonNull(listOperations);
    }

    @GetMapping
    @Operation(summary = "Get all ISIN to ticker symbol mappings")
    @Transactional(readOnly = true)
    public ResponseEntity<PaginatedResponseDto<TickerSymbolDto>> getTickerSymbols(
            @RequestParam(required = false, defaultValue = "") String isin,
            @RequestParam(required = false, defaultValue = "") String tickerSymbol,
            @RequestParam(required = false, defaultValue = "") String name,
            @RequestParam(required = false, defaultValue = "isin") String sortField,
            @RequestParam(required = false, defaultValue = "asc") String sortDir,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {
        List<TickerSymbolDto> data = filterAndSort(loadAll(),
            new TickerSymbolFilter(isin, tickerSymbol, name),
            new SortRequest(sortField, sortDir));
        return ResponseEntity.ok(listOperations.paginationHelper().paginate(data, page, pageSize));
    }

    @GetMapping("/export")
    @Operation(summary = "Export ticker symbols as CSV or Excel")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> exportTickerSymbols(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false, defaultValue = "") String isin,
            @RequestParam(required = false, defaultValue = "") String tickerSymbol,
            @RequestParam(required = false, defaultValue = "") String name,
            @RequestParam(required = false) String sortField,
            @RequestParam(defaultValue = "asc") String sortDir) {
        List<TickerSymbolDto> data = filterAndSort(loadAll(),
            new TickerSymbolFilter(isin, tickerSymbol, name),
            new SortRequest(sortField, sortDir));
        List<ExportColumn<TickerSymbolDto>> columns = List.of(
                new ExportColumn<>("ISIN", TickerSymbolDto::getIsin),
                new ExportColumn<>("Ticker Symbol", TickerSymbolDto::getTickerSymbol),
                new ExportColumn<>("Name", TickerSymbolDto::getName)
        );
        return listOperations.exportService().export(new ExportRequest<>(data, columns, format, "ticker-symbols"));
    }

    private List<TickerSymbolDto> filterAndSort(List<TickerSymbolDto> data, TickerSymbolFilter filter,
                                                        SortRequest sort) {
        if (!filter.isinFragment().isBlank()) {
            String lower = filter.isinFragment().toLowerCase();
            data = data.stream().filter(dto -> dto.getIsin() != null && dto.getIsin().value().toLowerCase().contains(lower)).toList();
        }
        if (!filter.tickerSymbolFragment().isBlank()) {
            String lower = filter.tickerSymbolFragment().toLowerCase();
            data = data.stream().filter(dto -> dto.getTickerSymbol() != null && dto.getTickerSymbol().toLowerCase().contains(lower)).toList();
        }
        if (!filter.nameFragment().isBlank()) {
            String lower = filter.nameFragment().toLowerCase();
            data = data.stream().filter(dto -> dto.getName() != null && dto.getName().toLowerCase().contains(lower)).toList();
        }
        if (sort.sortField() != null && !sort.sortField().isBlank()) {
            data = listOperations.sortHelper().sort(data, sort, SORT_FIELDS);
        }
        return data;
    }

    @SuppressWarnings("unchecked")
    private List<TickerSymbolDto> loadAll() {
        List<Object[]> rows = entityManager.createNativeQuery("""
            SELECT i.isin, ts.symbol,
                   (SELECT n.name FROM isin_name n WHERE n.isin_id = i.id ORDER BY n.id ASC LIMIT 1)
            FROM isin_ticker it
            JOIN isin i ON i.id = it.isin_id
            JOIN ticker_symbol ts ON ts.id = it.ticker_symbol_id
            ORDER BY i.isin ASC, ts.symbol ASC
            """).getResultList();

        return rows.stream()
            .map(row -> new TickerSymbolDto(new Isin((String) row[0]), (String) row[1], (String) row[2]))
            .toList();
    }
}