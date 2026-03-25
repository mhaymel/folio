package com.folio.controller;

import com.folio.dto.TickerSymbolDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManager;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ticker-symbols")
@Tag(name = "Ticker Symbols", description = "ISIN to ticker symbol mappings")
public class TickerSymbolController {

    private final EntityManager em;

    public TickerSymbolController(EntityManager em) {
        this.em = em;
    }

    @GetMapping
    @Operation(summary = "Get all ISIN to ticker symbol mappings")
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public ResponseEntity<List<TickerSymbolDto>> getTickerSymbols() {
        List<Object[]> rows = em.createNativeQuery("""
            SELECT i.isin, ts.symbol,
                   (SELECT n.name FROM isin_name n WHERE n.isin_id = i.id ORDER BY n.id ASC LIMIT 1)
            FROM isin_ticker it
            JOIN isin i ON i.id = it.isin_id
            JOIN ticker_symbol ts ON ts.id = it.ticker_symbol_id
            ORDER BY i.isin ASC, ts.symbol ASC
            """).getResultList();

        List<TickerSymbolDto> result = rows.stream()
            .map(r -> TickerSymbolDto.builder()
                .isin((String) r[0])
                .tickerSymbol((String) r[1])
                .name((String) r[2])
                .build())
            .toList();

        return ResponseEntity.ok(result);
    }
}

