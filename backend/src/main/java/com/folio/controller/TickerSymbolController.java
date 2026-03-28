package com.folio.controller;

import com.folio.dto.ExportRequest;
import com.folio.dto.PaginatedResponseDto;
import com.folio.dto.TickerSymbolDto;
import com.folio.service.ExportService;
import com.folio.service.PaginationHelper;
import com.folio.service.SortHelper;
import com.folio.dto.ExportColumn;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManager;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ticker-symbols")
@Tag(name = "Ticker Symbols", description = "ISIN to ticker symbol mappings")
public class TickerSymbolController {

    private static final Map<String, Comparator<TickerSymbolDto>> SORT_FIELDS = Map.of(
        "isin", SortHelper.text(TickerSymbolDto::getIsin),
        "tickerSymbol", SortHelper.text(TickerSymbolDto::getTickerSymbol),
        "name", SortHelper.text(TickerSymbolDto::getName)
    );

    private final EntityManager em;
    private final ExportService exportService;

    public TickerSymbolController(EntityManager em, ExportService exportService) {
        this.em = em;
        this.exportService = exportService;
    }

    @GetMapping
    @Operation(summary = "Get all ISIN to ticker symbol mappings")
    @Transactional(readOnly = true)
    public ResponseEntity<PaginatedResponseDto<TickerSymbolDto>> getTickerSymbols(
            @RequestParam(required = false, defaultValue = "isin") String sortField,
            @RequestParam(required = false, defaultValue = "asc") String sortDir,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {
        List<TickerSymbolDto> data = SortHelper.sort(loadAll(), sortField, sortDir, SORT_FIELDS);
        return ResponseEntity.ok(PaginationHelper.paginate(data, page, pageSize));
    }

    @GetMapping("/export")
    @Operation(summary = "Export ticker symbols as CSV or Excel")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> exportTickerSymbols(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) String sortField,
            @RequestParam(defaultValue = "asc") String sortDir) {
        List<TickerSymbolDto> data = loadAll();
        if (sortField != null && !sortField.isBlank()) {
            data = SortHelper.sort(data, sortField, sortDir, SORT_FIELDS);
        }
        List<ExportColumn<TickerSymbolDto>> columns = List.of(
                new ExportColumn<>("ISIN", TickerSymbolDto::getIsin),
                new ExportColumn<>("Ticker Symbol", TickerSymbolDto::getTickerSymbol),
                new ExportColumn<>("Name", TickerSymbolDto::getName)
        );
        return exportService.export(new ExportRequest<>(data, columns, format, "ticker-symbols"));
    }

    @SuppressWarnings("unchecked")
    private List<TickerSymbolDto> loadAll() {
        List<Object[]> rows = em.createNativeQuery("""
            SELECT i.isin, ts.symbol,
                   (SELECT n.name FROM isin_name n WHERE n.isin_id = i.id ORDER BY n.id ASC LIMIT 1)
            FROM isin_ticker it
            JOIN isin i ON i.id = it.isin_id
            JOIN ticker_symbol ts ON ts.id = it.ticker_symbol_id
            ORDER BY i.isin ASC, ts.symbol ASC
            """).getResultList();

        return rows.stream()
            .map(r -> TickerSymbolDto.builder()
                .isin((String) r[0])
                .tickerSymbol((String) r[1])
                .name((String) r[2])
                .build())
            .toList();
    }
}