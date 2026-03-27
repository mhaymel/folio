package com.folio.controller;

import com.folio.dto.ExportRequest;
import com.folio.dto.TickerSymbolDto;
import com.folio.service.ExportService;
import com.folio.dto.ExportColumn;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManager;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/ticker-symbols")
@Tag(name = "Ticker Symbols", description = "ISIN to ticker symbol mappings")
public class TickerSymbolController {

    private final EntityManager em;
    private final ExportService exportService;

    public TickerSymbolController(EntityManager em, ExportService exportService) {
        this.em = em;
        this.exportService = exportService;
    }

    @GetMapping
    @Operation(summary = "Get all ISIN to ticker symbol mappings")
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public ResponseEntity<List<TickerSymbolDto>> getTickerSymbols() {
        return ResponseEntity.ok(loadAll());
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
            data = sorted(data, sortField, sortDir);
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

    private static List<TickerSymbolDto> sorted(List<TickerSymbolDto> data, String field, String dir) {
        Comparator<TickerSymbolDto> cmp = switch (field) {
            case "isin" -> Comparator.comparing(TickerSymbolDto::getIsin, Comparator.nullsLast(String::compareToIgnoreCase));
            case "tickerSymbol" -> Comparator.comparing(TickerSymbolDto::getTickerSymbol, Comparator.nullsLast(String::compareToIgnoreCase));
            case "name" -> Comparator.comparing(TickerSymbolDto::getName, Comparator.nullsLast(String::compareToIgnoreCase));
            default -> null;
        };
        if (cmp == null) return data;
        if ("desc".equalsIgnoreCase(dir)) cmp = cmp.reversed();
        return data.stream().sorted(cmp).toList();
    }
}
